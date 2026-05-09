package com.vyg.eis.CRM.dto;

import com.vyg.eis.CRM.domain.CRM.enums.ActivityType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ActivityLogRequest {
    private ActivityType activityType;
    private String summary;
    private String outcome;
    private String nextAction;
    private LocalDate reminderDate;
    private Integer durationMinutes;
    private String performedBy;
}
