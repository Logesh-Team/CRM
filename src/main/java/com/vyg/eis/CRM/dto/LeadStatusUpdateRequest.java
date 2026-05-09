package com.vyg.eis.CRM.dto;

import com.vyg.eis.CRM.domain.CRM.enums.LeadStatus;
import lombok.Data;

@Data
public class LeadStatusUpdateRequest {
    private LeadStatus newStatus;
    private String performedBy;
    private String summary;
}
