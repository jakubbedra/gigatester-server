package com.konfyrm.gigatester.calendar.service;

import com.konfyrm.gigatester.calendar.domain.dto.request.CalendarGroupRequest;
import com.konfyrm.gigatester.calendar.domain.dto.request.InviteMemberRequest;
import com.konfyrm.gigatester.calendar.domain.dto.response.CalendarGroupMemberResponse;
import com.konfyrm.gigatester.calendar.domain.dto.response.CalendarGroupResponse;
import com.konfyrm.gigatester.calendar.domain.dto.response.CalendarInviteResponse;
import com.konfyrm.gigatester.calendar.domain.dto.response.CalendarJoinPreviewResponse;
import com.konfyrm.gigatester.calendar.domain.entity.CalendarGroup;
import com.konfyrm.gigatester.calendar.domain.entity.CalendarGroupMember;
import com.konfyrm.gigatester.calendar.repository.CalendarEventRepository;
import com.konfyrm.gigatester.calendar.repository.CalendarGroupMemberRepository;
import com.konfyrm.gigatester.calendar.repository.CalendarGroupRepository;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalendarGroupService {

    private final CalendarGroupRepository calendarGroupRepository;
    private final CalendarGroupMemberRepository memberRepository;
    private final CalendarEventRepository eventRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public CalendarGroupService(
            CalendarGroupRepository calendarGroupRepository,
            CalendarGroupMemberRepository memberRepository,
            CalendarEventRepository eventRepository,
            UserRepository userRepository,
            PermissionService permissionService
    ) {
        this.calendarGroupRepository = calendarGroupRepository;
        this.memberRepository = memberRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    public boolean canManage(CalendarGroup group, User user) {
        return permissionService.isAdmin(user) || group.getOwner().getId().equals(user.getId());
    }

    public boolean canView(CalendarGroup group, User user) {
        if (canManage(group, user)) return true;
        return memberRepository.findByCalendarGroup_IdAndUser_Id(group.getId(), user.getId())
                .filter(CalendarGroupService::isAccepted)
                .isPresent();
    }

    /** Accepted (or legacy, pre-invite-flow) memberships only — a pending invite doesn't grant visibility yet. */
    private static boolean isAccepted(CalendarGroupMember member) {
        return !Boolean.FALSE.equals(member.getAccepted());
    }

    /** Admins see every group; everyone else sees groups they own or have accepted an invite into. */
    public List<CalendarGroupResponse> getVisibleGroups(User user) {
        List<CalendarGroup> groups;
        if (permissionService.isAdmin(user)) {
            groups = calendarGroupRepository.findAll();
        } else {
            Set<UUID> memberGroupIds = memberRepository.findByUser_Id(user.getId()).stream()
                    .filter(CalendarGroupService::isAccepted)
                    .map(m -> m.getCalendarGroup().getId())
                    .collect(Collectors.toSet());
            groups = calendarGroupRepository.findAll().stream()
                    .filter(g -> g.getOwner().getId().equals(user.getId()) || memberGroupIds.contains(g.getId()))
                    .collect(Collectors.toList());
        }
        return groups.stream().map(g -> toResponse(g, user)).collect(Collectors.toList());
    }

    /** This user's pending (not yet accepted or declined) calendar group invites. */
    public List<CalendarInviteResponse> getMyInvites(User user) {
        return memberRepository.findByUser_Id(user.getId()).stream()
                .filter(m -> Boolean.FALSE.equals(m.getAccepted()))
                .map(m -> CalendarInviteResponse.builder()
                        .id(m.getId())
                        .groupId(m.getCalendarGroup().getId())
                        .groupName(m.getCalendarGroup().getName())
                        .ownerUsername(m.getCalendarGroup().getOwner().getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptInvite(UUID memberId, User user) {
        CalendarGroupMember member = findMyPendingInvite(memberId, user);
        member.setAccepted(true);
        memberRepository.save(member);
    }

    @Transactional
    public void declineInvite(UUID memberId, User user) {
        CalendarGroupMember member = findMyPendingInvite(memberId, user);
        memberRepository.delete(member);
    }

    private CalendarGroupMember findMyPendingInvite(UUID memberId, User user) {
        CalendarGroupMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found: " + memberId));
        if (!member.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return member;
    }

    @Transactional
    public CalendarGroupResponse createGroup(CalendarGroupRequest request, User user) {
        permissionService.require(permissionService.isStaff(user));
        CalendarGroup group = calendarGroupRepository.save(CalendarGroup.builder()
                .name(request.getName())
                .owner(user)
                .build());
        return toResponse(group, user);
    }

    @Transactional
    public CalendarGroupResponse updateGroup(UUID groupId, CalendarGroupRequest request, User user) {
        CalendarGroup group = findGroup(groupId);
        permissionService.require(canManage(group, user));
        group.setName(request.getName());
        return toResponse(calendarGroupRepository.save(group), user);
    }

    @Transactional
    public void deleteGroup(UUID groupId, User user) {
        CalendarGroup group = findGroup(groupId);
        permissionService.require(canManage(group, user));
        eventRepository.deleteByCalendarGroup_Id(groupId);
        memberRepository.deleteByCalendarGroup_Id(groupId);
        calendarGroupRepository.delete(group);
    }

    public List<CalendarGroupMemberResponse> getMembers(UUID groupId, User user) {
        CalendarGroup group = findGroup(groupId);
        permissionService.require(canView(group, user));
        return memberRepository.findByCalendarGroup_Id(groupId).stream()
                .map(m -> CalendarGroupMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void inviteMember(UUID groupId, InviteMemberRequest request, User user) {
        CalendarGroup group = findGroup(groupId);
        permissionService.require(canManage(group, user));
        User invitee = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + request.getUsername()));
        if (memberRepository.findByCalendarGroup_IdAndUser_Id(groupId, invitee.getId()).isPresent()) {
            return;
        }
        memberRepository.save(CalendarGroupMember.builder()
                .calendarGroup(group)
                .user(invitee)
                .accepted(false)
                .build());
    }

    @Transactional
    public void removeMember(UUID groupId, UUID userId, User user) {
        CalendarGroup group = findGroup(groupId);
        permissionService.require(canManage(group, user));
        memberRepository.deleteByCalendarGroup_IdAndUser_Id(groupId, userId);
    }

    /** Returns the group's existing invite token, generating one on first use. Manager-only. */
    @Transactional
    public String getOrCreateInviteToken(UUID groupId, User user) {
        CalendarGroup group = findGroup(groupId);
        permissionService.require(canManage(group, user));
        if (group.getInviteToken() == null) {
            group.setInviteToken(UUID.randomUUID().toString());
            calendarGroupRepository.save(group);
        }
        return group.getInviteToken();
    }

    /** Invalidates the old link (if any) and issues a new one. Manager-only. */
    @Transactional
    public String regenerateInviteToken(UUID groupId, User user) {
        CalendarGroup group = findGroup(groupId);
        permissionService.require(canManage(group, user));
        group.setInviteToken(UUID.randomUUID().toString());
        calendarGroupRepository.save(group);
        return group.getInviteToken();
    }

    public CalendarJoinPreviewResponse previewInvite(String token, User user) {
        CalendarGroup group = findGroupByToken(token);
        return CalendarJoinPreviewResponse.builder()
                .groupName(group.getName())
                .ownerUsername(group.getOwner().getUsername())
                .alreadyMember(canView(group, user))
                .build();
    }

    /** Joins the current user into the group behind this invite link/QR code as an accepted member. */
    @Transactional
    public CalendarGroupResponse joinByToken(String token, User user) {
        CalendarGroup group = findGroupByToken(token);
        if (!canView(group, user)) {
            memberRepository.findByCalendarGroup_IdAndUser_Id(group.getId(), user.getId())
                    .ifPresentOrElse(
                            member -> { member.setAccepted(true); memberRepository.save(member); },
                            () -> memberRepository.save(CalendarGroupMember.builder()
                                    .calendarGroup(group)
                                    .user(user)
                                    .accepted(true)
                                    .build())
                    );
        }
        return toResponse(group, user);
    }

    private CalendarGroup findGroupByToken(String token) {
        return calendarGroupRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite link not found or expired"));
    }

    CalendarGroup findGroup(UUID groupId) {
        return calendarGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar group not found: " + groupId));
    }

    private CalendarGroupResponse toResponse(CalendarGroup group, User user) {
        boolean owner = group.getOwner().getId().equals(user.getId());
        boolean canManage = owner || permissionService.isAdmin(user);
        return CalendarGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .ownerId(group.getOwner().getId())
                .ownerUsername(group.getOwner().getUsername())
                .owner(owner)
                .canManage(canManage)
                .memberCount((int) memberRepository.findByCalendarGroup_Id(group.getId()).stream()
                        .filter(CalendarGroupService::isAccepted).count())
                .inviteToken(canManage ? group.getInviteToken() : null)
                .build();
    }

}
