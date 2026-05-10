package com.vyg.eis.CRM.dto;

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
public class LoginResponse {
    private String token;
    private UUID userId;
    private String name;
    private String email;
    private UserRole role;
}
