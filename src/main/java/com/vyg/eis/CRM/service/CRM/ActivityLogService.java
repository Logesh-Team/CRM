package com.vyg.eis.CRM.service.CRM;

import com.vyg.eis.CRM.domain.CRM.ActivityLog;
import com.vyg.eis.CRM.domain.CRM.Lead;
import com.vyg.eis.CRM.domain.CRM.enums.ActivityType;
import com.vyg.eis.CRM.domain.CRM.enums.LeadStatus;
import com.vyg.eis.CRM.dto.ActivityLogRequest;
import com.vyg.eis.CRM.repository.CRM.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLog logActivity(Lead lead, ActivityType type, String summary, String performedBy) {
        ActivityLog log = ActivityLog.builder()
                .leadId(lead.getId())
                .activityType(type)
                .summary(summary)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build();
        return activityLogRepository.save(log);
    }

    public ActivityLog logStatusChange(Lead lead, LeadStatus oldStatus, LeadStatus newStatus,
                                       String summary, String performedBy) {
        String defaultSummary = String.format("Status changed from %s to %s", oldStatus, newStatus);
        ActivityLog log = ActivityLog.builder()
                .leadId(lead.getId())
                .activityType(ActivityType.STATUS_CHANGE)
                .summary(summary != null && !summary.isBlank() ? summary : defaultSummary)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build();
        return activityLogRepository.save(log);
    }

    public ActivityLog logFromRequest(Lead lead, ActivityLogRequest request) {
        ActivityLog log = ActivityLog.builder()
                .leadId(lead.getId())
                .activityType(request.getActivityType())
                .summary(request.getSummary())
                .outcome(request.getOutcome())
                .nextAction(request.getNextAction())
                .reminderDate(request.getReminderDate())
                .durationMinutes(request.getDurationMinutes())
                .performedBy(request.getPerformedBy())
                .performedAt(LocalDateTime.now())
                .build();
        return activityLogRepository.save(log);
    }

    public List<ActivityLog> getLeadActivities(Long leadId) {
        return activityLogRepository.findByLeadIdOrderByPerformedAtDesc(leadId);
    }
}
