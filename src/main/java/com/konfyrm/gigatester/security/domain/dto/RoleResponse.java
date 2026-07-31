package com.konfyrm.gigatester.security.domain.dto;

import com.konfyrm.gigatester.security.domain.Permission;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private UUID id;

    private String name;

    private Set<Permission> permissions;

}
