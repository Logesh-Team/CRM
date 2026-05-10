package com.vyg.eis.CRM.config;

import com.vyg.eis.CRM.domain.CRM.enums.Permission;
import com.vyg.eis.CRM.domain.CRM.enums.UserRole;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class RolePermissionConfig {

    private static final Map<UserRole, Set<Permission>> ROLE_PERMISSIONS = new EnumMap<>(UserRole.class);

    static {
        ROLE_PERMISSIONS.put(UserRole.SUPER_ADMIN, EnumSet.allOf(Permission.class));

        ROLE_PERMISSIONS.put(UserRole.SALES_MANAGER, EnumSet.of(
                Permission.LEAD_READ_ALL, Permission.LEAD_ASSIGN, Permission.LEAD_UPDATE_ALL,
                Permission.ACTIVITY_READ_ALL, Permission.DEMO_SCHEDULE,
                Permission.QUOTATION_APPROVE, Permission.QUOTATION_CREATE,
                Permission.DASHBOARD_TEAM, Permission.DASHBOARD_FULL,
                Permission.REPORT_TEAM, Permission.REPORT_FULL,
                Permission.USER_READ, Permission.CAMPAIGN_ANALYTICS
        ));

        ROLE_PERMISSIONS.put(UserRole.SALES_EXECUTIVE, EnumSet.of(
                Permission.LEAD_CREATE, Permission.LEAD_READ_OWN, Permission.LEAD_UPDATE_OWN,
                Permission.ACTIVITY_LOG, Permission.ACTIVITY_READ_OWN,
                Permission.DEMO_SCHEDULE, Permission.QUOTATION_CREATE,
                Permission.DASHBOARD_OWN, Permission.REPORT_OWN
        ));

        ROLE_PERMISSIONS.put(UserRole.MARKETING_EXECUTIVE, EnumSet.of(
                Permission.CAMPAIGN_CREATE, Permission.CAMPAIGN_MANAGE, Permission.CAMPAIGN_ANALYTICS,
                Permission.LEAD_READ_ALL, Permission.LEAD_CREATE, Permission.DASHBOARD_OWN
        ));

        ROLE_PERMISSIONS.put(UserRole.SYSTEM_BOT, EnumSet.of(
                Permission.LEAD_CREATE, Permission.LEAD_UPDATE_ALL, Permission.ACTIVITY_LOG
        ));
    }

    private RolePermissionConfig() {}

    public static boolean hasPermission(UserRole role, Permission permission) {
        return ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class))
                               .contains(permission);
    }

    public static Set<Permission> getPermissions(UserRole role) {
        return Collections.unmodifiableSet(
                ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class))
        );
    }
}
