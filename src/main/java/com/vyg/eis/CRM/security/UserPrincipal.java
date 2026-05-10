package com.vyg.eis.CRM.security;

import com.vyg.eis.CRM.domain.CRM.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final UUID id;
    private final String email;
    private final UserRole role;
    private final String name;
}
