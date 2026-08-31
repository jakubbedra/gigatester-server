package com.konfyrm.gigatester.calendar.service;

import com.konfyrm.gigatester.calendar.domain.CalendarLinkType;
import com.konfyrm.gigatester.calendar.domain.dto.request.CalendarEventLinkRequest;
import com.konfyrm.gigatester.calendar.domain.dto.request.CalendarEventRequest;
import com.konfyrm.gigatester.calendar.domain.dto.response.CalendarEventLinkResponse;
import com.konfyrm.gigatester.calendar.domain.dto.response.CalendarEventResponse;
import com.konfyrm.gigatester.calendar.domain.entity.CalendarEvent;
import com.konfyrm.gigatester.calendar.domain.entity.CalendarEventLink;
import com.konfyrm.gigatester.calendar.domain.entity.CalendarGroup;
import com.konfyrm.gigatester.calendar.repository.CalendarEventRepository;
import com.konfyrm.gigatester.calendar.repository.CalendarGroupMemberRepository;
import com.konfyrm.gigatester.calendar.repository.CalendarGroupRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalendarEventService {

    private final CalendarEventRepository eventRepository;
    private final CalendarGroupRepository groupRepository;
    private final CalendarGroupMemberRepository memberRepository;
    private final TestRepository testRepository;
    private final CrosswordRepository crosswordRepository;
    private final PermissionService permissionService;
    private final CalendarGroupService calendarGroupService;

    public CalendarEventService(
            CalendarEventRepository eventRepository,
            CalendarGroupRepository groupRepository,
            CalendarGroupMemberRepository memberRepository,
            TestRepository testRepository,
            CrosswordRepository crosswordRepository,
            PermissionService permissionService,
            CalendarGroupService calendarGroupService
    ) {
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.testRepository = testRepository;
        this.crosswordRepository = crosswordRepository;
        this.permissionService = permissionService;
        this.calendarGroupService = calendarGroupService;
    }

    /** Events from every group this user can see (owned, member of, or all if admin), optionally narrowed to specific groups and a date window. */
    public List<CalendarEventResponse> getEvents(User user, List<UUID> groupIds, LocalDateTime from, LocalDateTime to) {
        Set<UUID> visibleGroupIds = visibleGroupIds(user);
        if (groupIds != null && !groupIds.isEmpty()) {
            visibleGroupIds.retainAll(groupIds);
        }
        if (visibleGroupIds.isEmpty()) return List.of();

        List<CalendarEvent> events = (from != null && to != null)
                ? eventRepository.findByCalendarGroup_IdInAndStartTimeBetween(visibleGroupIds, from, to)
                : eventRepository.findByCalendarGroup_IdIn(visibleGroupIds);

        return events.stream().map(e -> toResponse(e, user)).collect(Collectors.toList());
    }

    @Transactional
    public CalendarEventResponse createEvent(CalendarEventRequest request, User user) {
        CalendarGroup group = calendarGroupService.findGroup(request.getCalendarGroupId());
        permissionService.require(calendarGroupService.canManage(group, user));
        List<CalendarEventLink> links = toLinks(request.getLinks());

        CalendarEvent event = CalendarEvent.builder()
                .calendarGroup(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .links(links)
                .createdBy(user)
                .build();
        return toResponse(eventRepository.save(event), user);
    }

    @Transactional
    public CalendarEventResponse updateEvent(UUID eventId, CalendarEventRequest request, User user) {
        CalendarEvent event = findEvent(eventId);
        permissionService.require(calendarGroupService.canManage(event.getCalendarGroup(), user));

        CalendarGroup targetGroup = request.getCalendarGroupId().equals(event.getCalendarGroup().getId())
                ? event.getCalendarGroup()
                : calendarGroupService.findGroup(request.getCalendarGroupId());
        if (targetGroup != event.getCalendarGroup()) {
            permissionService.require(calendarGroupService.canManage(targetGroup, user));
        }
        List<CalendarEventLink> links = toLinks(request.getLinks());

        event.setCalendarGroup(targetGroup);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        // Clear any legacy single-link fields once the event is saved through the new
        // multi-link path, so toResponse's legacy fallback doesn't re-surface a stale link.
        event.setLinkType(null);
        event.setLinkId(null);
        event.getLinks().clear();
        event.getLinks().addAll(links);
        return toResponse(eventRepository.save(event), user);
    }

    @Transactional
    public void deleteEvent(UUID eventId, User user) {
        CalendarEvent event = findEvent(eventId);
        permissionService.require(calendarGroupService.canManage(event.getCalendarGroup(), user));
        eventRepository.delete(event);
    }

    private List<CalendarEventLink> toLinks(List<CalendarEventLinkRequest> requested) {
        if (requested == null || requested.isEmpty()) return new ArrayList<>();
        List<CalendarEventLink> links = new ArrayList<>();
        for (CalendarEventLinkRequest r : requested) {
            if (r.getLinkType() == null || r.getLinkId() == null) continue;
            validateLink(r.getLinkType(), r.getLinkId());
            links.add(CalendarEventLink.builder().linkType(r.getLinkType()).linkId(r.getLinkId()).build());
        }
        return links;
    }

    private void validateLink(CalendarLinkType linkType, UUID linkId) {
        boolean exists = switch (linkType) {
            case TEST -> testRepository.existsById(linkId);
            case CROSSWORD -> crosswordRepository.existsById(linkId);
        };
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Linked " + linkType + " not found: " + linkId);
        }
    }

    private Set<UUID> visibleGroupIds(User user) {
        if (permissionService.isAdmin(user)) {
            return groupRepository.findAll().stream().map(CalendarGroup::getId).collect(Collectors.toSet());
        }
        Set<UUID> ids = memberRepository.findByUser_Id(user.getId()).stream()
                .filter(m -> !Boolean.FALSE.equals(m.getAccepted()))
                .map(m -> m.getCalendarGroup().getId())
                .collect(Collectors.toSet());
        groupRepository.findByOwner_Id(user.getId()).forEach(g -> ids.add(g.getId()));
        return ids;
    }

    private CalendarEvent findEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar event not found: " + eventId));
    }

    private CalendarEventResponse toResponse(CalendarEvent event, User user) {
        List<CalendarEventLink> links = event.getLinks();
        // Legacy fallback: events saved before multi-link support only have the old
        // single linkType/linkId columns populated, with an empty links collection.
        if ((links == null || links.isEmpty()) && event.getLinkType() != null && event.getLinkId() != null) {
            links = List.of(CalendarEventLink.builder().linkType(event.getLinkType()).linkId(event.getLinkId()).build());
        }
        List<CalendarEventLinkResponse> linkResponses = links == null ? List.of() : links.stream()
                .map(l -> CalendarEventLinkResponse.builder()
                        .linkType(l.getLinkType())
                        .linkId(l.getLinkId())
                        .linkName(resolveLinkName(l.getLinkType(), l.getLinkId()))
                        .build())
                .collect(Collectors.toList());

        return CalendarEventResponse.builder()
                .id(event.getId())
                .calendarGroupId(event.getCalendarGroup().getId())
                .calendarGroupName(event.getCalendarGroup().getName())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .links(linkResponses)
                .canManage(calendarGroupService.canManage(event.getCalendarGroup(), user))
                .build();
    }

    private String resolveLinkName(CalendarLinkType linkType, UUID linkId) {
        if (linkType == null || linkId == null) return null;
        return switch (linkType) {
            case TEST -> testRepository.findById(linkId).map(com.konfyrm.gigatester.tests.domain.entity.Test::getName).orElse(null);
            case CROSSWORD -> crosswordRepository.findById(linkId).map(com.konfyrm.gigatester.crosswords.domain.entity.Crossword::getName).orElse(null);
        };
    }

}
