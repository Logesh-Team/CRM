package com.vyg.eis.CRM.dto;

import lombok.Data;

@Data
public class LeadAssignRequest {
    private String assignedTo;
    private String assignedManager;
    private String territory;
    private String updatedBy;
}
