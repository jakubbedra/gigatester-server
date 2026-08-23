package com.konfyrm.gigatester.users.domain.dto.response;

import com.konfyrm.gigatester.security.domain.Permission;
import com.konfyrm.gigatester.users.domain.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private UserRole role;
    private UUID assignedRoleId;
    private String assignedRoleName;
    private Set<Permission> permissions;
    private String profilePictureUrl;
    private String bio;
    private boolean showFavouritesTab;
}
