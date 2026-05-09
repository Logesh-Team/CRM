package com.vyg.eis.CRM.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiLeadBulkCreateRequest {
    private List<AiLeadResult> leads;
    private String assignedTo;
    private String createdBy;
}
