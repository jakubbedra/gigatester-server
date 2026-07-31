package com.konfyrm.gigatester.security.domain.dto;

import com.konfyrm.gigatester.security.domain.Permission;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    private String name;

    private Set<Permission> permissions;

}
