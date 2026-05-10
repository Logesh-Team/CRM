package com.vyg.eis.CRM.domain.CRM.enums;

public enum Permission {
    // User Management
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,
    USER_ROLE_CHANGE,

    // Lead Management
    LEAD_CREATE,
    LEAD_READ_OWN,
    LEAD_READ_ALL,
    LEAD_UPDATE_OWN,
    LEAD_UPDATE_ALL,
    LEAD_DELETE,
    LEAD_ASSIGN,

    // Activity
    ACTIVITY_LOG,
    ACTIVITY_READ_OWN,
    ACTIVITY_READ_ALL,

    // Demo & Quotation
    DEMO_SCHEDULE,
    QUOTATION_CREATE,
    QUOTATION_APPROVE,

    // Campaign
    CAMPAIGN_CREATE,
    CAMPAIGN_MANAGE,
    CAMPAIGN_ANALYTICS,

    // Reports & Dashboard
    DASHBOARD_OWN,
    DASHBOARD_TEAM,
    DASHBOARD_FULL,
    REPORT_OWN,
    REPORT_TEAM,
    REPORT_FULL,

    // System
    API_KEY_MANAGE,
    SYSTEM_CONFIG
}
