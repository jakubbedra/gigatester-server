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
import com.konfyrm.gigatester.coding.domain.entity.CodingTask;
import com.konfyrm.gigatester.coding.repository.CodingTaskRepository;
import com.konfyrm.gigatester.graphs.repository.GraphRepository;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.domain.entity.TestType;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private final GraphRepository graphRepository;
    private final CodingTaskRepository codingTaskRepository;
    private final PermissionService permissionService;
    private final CalendarGroupService calendarGroupService;
    private final SubjectGroupAccessService accessService;
    private final SubjectRepository subjectRepository;
    private final SubjectGroupRepository subjectGroupRepository;

    public CalendarEventService(
            CalendarEventRepository eventRepository,
            CalendarGroupRepository groupRepository,
            CalendarGroupMemberRepository memberRepository,
            TestRepository testRepository,
            CrosswordRepository crosswordRepository,
            GraphRepository graphRepository,
            CodingTaskRepository codingTaskRepository,
            PermissionService permissionService,
            CalendarGroupService calendarGroupService,
            SubjectGroupAccessService accessService,
            SubjectRepository subjectRepository,
            SubjectGroupRepository subjectGroupRepository
    ) {
        this.codingTaskRepository = codingTaskRepository;
        this.accessService = accessService;
        this.subjectRepository = subjectRepository;
        this.subjectGroupRepository = subjectGroupRepository;
        this.graphRepository = graphRepository;
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
        List<CalendarEventResponse> result = new ArrayList<>(storedEvents(user, groupIds, from, to));
        result.addAll(examEvents(user, from, to));
        return result;
    }

    /**
     * Scheduled exams show up on their own for everyone who can reach them: students through the
     * subject groups they have approved access to, staff for the exams they can manage. They are
     * generated here rather than stored, so they follow the exam's schedule with no extra upkeep.
     */
    private List<CalendarEventResponse> examEvents(User user, LocalDateTime from, LocalDateTime to) {
        boolean staff = permissionService.isStaff(user);
        List<CalendarEventResponse> events = new ArrayList<>(scheduledTestEvents(user, from, to, staff));
        for (CodingTask task : codingTaskRepository.findByStartAtIsNotNull()) {
            if (!task.isScheduled()) continue;
            boolean visible = staff
                    ? permissionService.hasCodingTaskPermission(user, task.getId(), Permission.CODING_WRITE)
                    : accessService.hasAccessToCodingTask(task.getId(), user);
            LocalDateTime start = LocalDateTime.ofInstant(task.getStartAt(), ZoneOffset.UTC);
            if (!visible || (from != null && to != null && (start.isBefore(from) || start.isAfter(to)))) continue;
            events.add(CalendarEventResponse.builder()
                    .id(task.getId())
                    .calendarGroupName("")
                    .title(task.getName())
                    .startTime(start)
                    .endTime(LocalDateTime.ofInstant(task.getWindowEnd(), ZoneOffset.UTC))
                    .links(List.of(CalendarEventLinkResponse.builder()
                            .linkType(CalendarLinkType.CODING_TASK)
                            .linkId(task.getId())
                            .linkName(task.getName())
                            .build()))
                    .canManage(false)
                    .exam(true)
                    .startInstant(task.getStartAt())
                    .endInstant(task.getWindowEnd())
                    .build());
        }
        return events;
    }

    private List<CalendarEventResponse> scheduledTestEvents(User user, LocalDateTime from, LocalDateTime to, boolean staff) {
        return testRepository.findByType(TestType.SCHEDULED_EXAM).stream()
                .filter(t -> t.getStartAt() != null && t.getWindowEnd() != null)
                .filter(t -> staff
                        ? permissionService.hasTestPermission(user, t.getId(), Permission.TESTS_WRITE)
                        : accessService.hasAccessToTest(t.getId(), user))
                .filter(t -> {
                    LocalDateTime start = LocalDateTime.ofInstant(t.getStartAt(), ZoneOffset.UTC);
                    return from == null || to == null || (!start.isBefore(from) && !start.isAfter(to));
                })
                .map(t -> toExamEvent(t, user, staff))
                .collect(Collectors.toList());
    }

    private CalendarEventResponse toExamEvent(Test test, User user, boolean staff) {
        String groupNames = subjectRepository.findByTests_Id(test.getId()).stream()
                .flatMap(s -> subjectGroupRepository.findByTests_Id(s.getId()).stream())
                .filter(g -> staff || accessService.hasAccess(g.getId(), user))
                .map(SubjectGroup::getName)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        return CalendarEventResponse.builder()
                .id(test.getId())
                .calendarGroupName(groupNames)
                .title(test.getName())
                .description(null)
                .startTime(LocalDateTime.ofInstant(test.getStartAt(), ZoneOffset.UTC))
                .endTime(LocalDateTime.ofInstant(test.getWindowEnd(), ZoneOffset.UTC))
                .links(List.of(CalendarEventLinkResponse.builder()
                        .linkType(CalendarLinkType.TEST)
                        .linkId(test.getId())
                        .linkName(test.getName())
                        .build()))
                .canManage(false)
                .exam(true)
                .startInstant(test.getStartAt())
                .endInstant(test.getWindowEnd())
                .build();
    }

    private List<CalendarEventResponse> storedEvents(User user, List<UUID> groupIds, LocalDateTime from, LocalDateTime to) {
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
            case GRAPH -> graphRepository.existsById(linkId);
            case CODING_TASK -> codingTaskRepository.existsById(linkId);
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
            case GRAPH -> graphRepository.findById(linkId).map(com.konfyrm.gigatester.graphs.domain.entity.Graph::getName).orElse(null);
            case CODING_TASK -> codingTaskRepository.findById(linkId).map(CodingTask::getName).orElse(null);
        };
    }

}
