package com.konfyrm.gigatester.security.service;

import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.subjects.domain.entity.Subject;
import com.konfyrm.gigatester.subjects.domain.entity.SubjectGroup;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.domain.entity.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves whether a user has a given {@link Permission}, scoped to the
 * subject groups they own ({@code SubjectGroup.owners}). Admins bypass all
 * checks. A resource not yet attached to any subject can only be written to
 * by its creator (or an admin), since it can't be resolved to a group yet.
 */
@Service
public class PermissionService {

    private final SubjectRepository subjectRepository;
    private final SubjectGroupRepository subjectGroupRepository;
    private final TestRepository testRepository;
    private final CrosswordRepository crosswordRepository;

    public PermissionService(SubjectRepository subjectRepository,
                              SubjectGroupRepository subjectGroupRepository,
                              TestRepository testRepository,
                              CrosswordRepository crosswordRepository) {
        this.subjectRepository = subjectRepository;
        this.subjectGroupRepository = subjectGroupRepository;
        this.testRepository = testRepository;
        this.crosswordRepository = crosswordRepository;
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    /**
     * True if the user is staff in any capacity — admin, or holds any granted
     * permission via their assigned role. Replaces the old flat
     * {@code UserRole.MODERATOR} check now that tiers are purely role/
     * permission-based rather than a separate enum value.
     */
    public boolean isStaff(User user) {
        if (isAdmin(user)) return true;
        return user != null && user.getAssignedRole() != null && !user.getAssignedRole().getPermissions().isEmpty();
    }

    /** True if the user's assigned role grants this permission, regardless of group. */
    public boolean grantedByRole(User user, Permission permission) {
        if (isAdmin(user)) return true;
        if (user == null || user.getAssignedRole() == null) return false;
        return user.getAssignedRole().getPermissions().stream().anyMatch(permission::impliedBy);
    }

    public boolean hasGroupPermission(User user, UUID groupId, Permission permission) {
        if (isAdmin(user)) return true;
        if (!grantedByRole(user, permission)) return false;
        SubjectGroup group = subjectGroupRepository.findById(groupId).orElse(null);
        return group != null && ownsGroup(user, group);
    }

    public boolean hasSubjectPermission(User user, UUID subjectId, Permission permission) {
        if (isAdmin(user)) return true;
        if (!grantedByRole(user, permission)) return false;
        return subjectGroupRepository.findByTests_Id(subjectId).stream().anyMatch(g -> ownsGroup(user, g));
    }

    public boolean hasTestPermission(User user, UUID testId, Permission permission) {
        if (isAdmin(user)) return true;
        List<Subject> subjects = subjectRepository.findByTests_Id(testId);
        if (subjects.isEmpty()) {
            return grantedByRole(user, permission) && createdByUser(testRepository.findById(testId).orElse(null), user);
        }
        return grantedByRole(user, permission)
                && subjects.stream().anyMatch(s -> hasSubjectPermission(user, s.getId(), permission));
    }

    public boolean hasCrosswordPermission(User user, UUID crosswordId, Permission permission) {
        if (isAdmin(user)) return true;
        List<Subject> subjects = subjectRepository.findByCrosswords_Id(crosswordId);
        if (subjects.isEmpty()) {
            return grantedByRole(user, permission) && createdByUser(crosswordRepository.findById(crosswordId).orElse(null), user);
        }
        return grantedByRole(user, permission)
                && subjects.stream().anyMatch(s -> hasSubjectPermission(user, s.getId(), permission));
    }

    /** Can the user create a new resource of this type at all (before it's attached to a subject)? */
    public boolean canCreate(User user, Permission writePermission) {
        return grantedByRole(user, writePermission);
    }

    /**
     * The flat set of permissions this user is granted, ungrouped/unscoped
     * (i.e. ignoring which subject groups they own). Exposed to the frontend
     * so it can decide what to show without a group-aware server round trip
     * per UI element — actual writes are still enforced server-side, scoped.
     */
    public Set<Permission> effectivePermissions(User user) {
        if (isAdmin(user)) {
            return EnumSet.allOf(Permission.class);
        }
        if (user == null || user.getAssignedRole() == null) {
            return EnumSet.noneOf(Permission.class);
        }
        EnumSet<Permission> resolved = EnumSet.noneOf(Permission.class);
        for (Permission candidate : Permission.values()) {
            if (grantedByRole(user, candidate)) {
                resolved.add(candidate);
            }
        }
        return resolved;
    }

    public void require(boolean allowed) {
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private boolean ownsGroup(User user, SubjectGroup group) {
        return user != null && group.getOwners().stream().anyMatch(o -> o.getId().equals(user.getId()));
    }

    private boolean createdByUser(Test test, User user) {
        return test != null && test.getCreatedBy() != null && user != null && test.getCreatedBy().getId().equals(user.getId());
    }

    private boolean createdByUser(Crossword crossword, User user) {
        return crossword != null && crossword.getCreatedBy() != null && user != null && crossword.getCreatedBy().getId().equals(user.getId());
    }

}
