package com.vyg.eis.CRM.permission;

import com.vyg.eis.CRM.config.RolePermissionConfig;
import com.vyg.eis.CRM.domain.CRM.enums.Permission;
import com.vyg.eis.CRM.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class PermissionUtils {

    private PermissionUtils() {}

    public static boolean currentUserHas(Permission permission) {
        UserPrincipal principal = getCurrentUser();
        return principal != null && RolePermissionConfig.hasPermission(principal.getRole(), permission);
    }

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }
}
