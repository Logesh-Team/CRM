package com.vyg.eis.CRM.dto;

import com.vyg.eis.CRM.domain.CRM.UserEntity;
import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {

    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private Boolean isActive;

    public static UserSummaryDTO from(UserEntity user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }
}
