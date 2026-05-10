package com.vyg.eis.CRM.dto;

import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRoleChangeRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "New role is required")
    private UserRole newRole;

    private String reason;
}
