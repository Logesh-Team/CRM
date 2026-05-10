package com.vyg.eis.CRM.dto;

import com.vyg.eis.CRM.config.RolePermissionConfig;
import com.vyg.eis.CRM.domain.CRM.UserEntity;
import com.vyg.eis.CRM.domain.CRM.enums.Permission;
import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private String phone;
    private String designation;
    private String department;
    private String profilePicture;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Set<Permission> permissions;

    public static UserDTO from(UserEntity user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .designation(user.getDesignation())
                .department(user.getDepartment())
                .profilePicture(user.getProfilePicture())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .permissions(RolePermissionConfig.getPermissions(user.getRole()))
                .build();
    }
}
