package com.vyg.eis.CRM.domain.CRM;

import com.vyg.eis.CRM.domain.CRM.enums.LeadGrade;
import com.vyg.eis.CRM.domain.CRM.enums.LeadPriority;
import com.vyg.eis.CRM.domain.CRM.enums.LeadSource;
import com.vyg.eis.CRM.domain.CRM.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_id", unique = true)
    private String leadId;

    // Identity
    @Column(name = "company_name")
    private String companyName;

    @Column(name = "industry_type")
    private String industryType;

    @Column(name = "sub_industry")
    private String subIndustry;

    @Column(name = "company_size")
    private String companySize;

    // Location
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;
    private String state;

    @Column(name = "pin_code")
    private String pinCode;

    private String country;

    @Column(name = "google_maps_link")
    private String googleMapsLink;

    // Contact
    @Column(name = "primary_contact_name")
    private String primaryContactName;

    private String designation;
    private String mobile;

    @Column(name = "alternate_mobile")
    private String alternateMobile;

    private String email;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    // Source
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_source")
    private LeadSource leadSource;

    @Column(name = "campaign_name")
    private String campaignName;

    // Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_grade")
    private LeadGrade leadGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_status")
    private LeadStatus leadStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_priority")
    private LeadPriority leadPriority;

    // Assignment
    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "assigned_manager")
    private String assignedManager;

    private String territory;

    // Financial
    @Column(name = "estimated_deal_value", precision = 15, scale = 2)
    private BigDecimal estimatedDealValue;

    @Column(name = "expected_revenue_month")
    private String expectedRevenueMonth;

    @Column(name = "product_interested")
    private String productInterested;

    // Tracking
    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @Column(name = "days_since_last_contact")
    private Integer daysSinceLastContact;

    // Notes
    @Column(name = "lead_description", columnDefinition = "TEXT")
    private String leadDescription;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    private String tags;

    @Column(name = "competitor_info")
    private String competitorInfo;

    // System
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "is_active")
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.leadStatus == null) {
            this.leadStatus = LeadStatus.NEW;
        }
        if (this.daysSinceLastContact == null) {
            this.daysSinceLastContact = 0;
        }
        if (this.lastActivityDate == null) {
            this.lastActivityDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
