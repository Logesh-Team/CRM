package com.vyg.eis.CRM.domain.CRM;

import com.vyg.eis.CRM.domain.CRM.enums.ActivityType;
import com.vyg.eis.CRM.domain.CRM.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_id")
    private Long leadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type")
    private ActivityType activityType;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String outcome;

    @Column(name = "next_action")
    private String nextAction;

    @Column(name = "reminder_date")
    private LocalDate reminderDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private LeadStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private LeadStatus newStatus;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @PrePersist
    protected void onCreate() {
        if (this.performedAt == null) {
            this.performedAt = LocalDateTime.now();
        }
    }
}
