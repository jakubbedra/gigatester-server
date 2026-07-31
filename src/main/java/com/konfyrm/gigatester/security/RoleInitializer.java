package com.konfyrm.gigatester.security;

import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.security.domain.Role;
import com.konfyrm.gigatester.security.repository.RoleRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.domain.entity.UserRole;
import com.konfyrm.gigatester.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * One-time conversion of the old flat {@link UserRole#MODERATOR} into the new
 * permission system: seeds a default "Moderator" role (SUBJECTS_WRITE, which
 * implies write access to tests/crosswords too), assigns it to any existing
 * moderator who doesn't yet have a role, and demotes them to {@code USER}.
 * Tiers are now purely role/permission-based — {@code UserRole} only
 * distinguishes ADMIN from everyone else. Idempotent — safe to run on every
 * startup.
 *
 * Note this is a behavior change, not just a relabeling: previously MODERATOR
 * meant unscoped global access; the converted role only grants access within
 * subject groups the user owns ({@code SubjectGroup.owners}), which is the
 * point of the new system.
 */
@Component
@RequiredArgsConstructor
@Order(1)
public class RoleInitializer implements ApplicationRunner {

    private static final String DEFAULT_MODERATOR_ROLE_NAME = "Moderator";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        Role moderatorRole = roleRepository.findByName(DEFAULT_MODERATOR_ROLE_NAME).orElseGet(() -> {
            HashSet<Permission> permissions = new HashSet<>();
            permissions.add(Permission.SUBJECTS_WRITE);
            return roleRepository.save(Role.builder()
                    .name(DEFAULT_MODERATOR_ROLE_NAME)
                    .permissions(permissions)
                    .build());
        });

        for (User user : userRepository.findAll()) {
            if (user.getRole() == UserRole.MODERATOR) {
                if (user.getAssignedRole() == null) {
                    user.setAssignedRole(moderatorRole);
                }
                user.setRole(UserRole.USER);
                userRepository.save(user);
            }
        }
    }
}
