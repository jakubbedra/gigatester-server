package com.konfyrm.gigatester.users.service;

import com.konfyrm.gigatester.comments.repository.CommentRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordStateRepository;
import com.konfyrm.gigatester.metrics.repository.DailyStreakRepository;
import com.konfyrm.gigatester.metrics.repository.UserQuestionStatRepository;
import com.konfyrm.gigatester.metrics.repository.UserTestStatRepository;
import com.konfyrm.gigatester.security.JwtService;
import com.konfyrm.gigatester.security.domain.Role;
import com.konfyrm.gigatester.security.repository.RoleRepository;
import com.konfyrm.gigatester.security.service.PermissionService;
import com.konfyrm.gigatester.subjects.repository.SubjectGroupAccessRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import com.konfyrm.gigatester.tests.repository.TestStateRepository;
import com.konfyrm.gigatester.users.domain.dto.request.RegisterRequest;
import com.konfyrm.gigatester.users.domain.dto.response.AuthResponse;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import com.konfyrm.gigatester.users.domain.entity.User;
import com.konfyrm.gigatester.users.domain.entity.UserRole;
import com.konfyrm.gigatester.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CommentRepository commentRepository;
    private final CrosswordStateRepository crosswordStateRepository;
    private final TestStateRepository testStateRepository;
    private final UserTestStatRepository userTestStatRepository;
    private final UserQuestionStatRepository userQuestionStatRepository;
    private final DailyStreakRepository dailyStreakRepository;
    private final SubjectGroupAccessRepository subjectGroupAccessRepository;
    private final SubjectRepository subjectRepository;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();
        userRepository.save(user);
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .user(toResponse(user))
                .build();
    }

    public AuthResponse login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .user(toResponse(user))
                .build();
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Cannot delete admin");
        }
        // Anonymize comments (preserve subject discussion, just remove author link)
        commentRepository.anonymizeByUserId(id);
        // Remove from subject author lists
        subjectRepository.removeUserFromAllAuthors(id);
        // Delete access requests
        subjectGroupAccessRepository.deleteAll(subjectGroupAccessRepository.findByUser_Id(id));
        // Delete metrics
        userQuestionStatRepository.deleteByUser_Id(id);
        userTestStatRepository.deleteByUser_Id(id);
        dailyStreakRepository.findByUserId(id).ifPresent(dailyStreakRepository::delete);
        // Delete game states
        crosswordStateRepository.deleteByUser_Id(id);
        testStateRepository.deleteByUser_Id(id);
        // Delete the user
        userRepository.delete(user);
    }

    public UserResponse getMe(User user) {
        return toResponse(user);
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponse updateProfilePicture(User user, String url) {
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse updateBio(User user, String bio) {
        user.setBio(bio);
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse assignRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (roleId == null) {
            user.setAssignedRole(null);
        } else {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            user.setAssignedRole(role);
        }
        userRepository.save(user);
        return toResponse(user);
    }

    public String generatePasswordResetToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jwtService.generatePasswordResetToken(user);
    }

    public void resetPassword(String token, String newPassword) {
        if (!jwtService.isPasswordResetToken(token)) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void ensureAdminExists(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User admin = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .assignedRoleId(user.getAssignedRole() != null ? user.getAssignedRole().getId() : null)
                .assignedRoleName(user.getAssignedRole() != null ? user.getAssignedRole().getName() : null)
                .permissions(permissionService.effectivePermissions(user))
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .build();
    }
}
