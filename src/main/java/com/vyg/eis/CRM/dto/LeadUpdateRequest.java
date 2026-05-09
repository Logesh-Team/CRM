package com.vyg.eis.CRM.dto;

import com.vyg.eis.CRM.domain.CRM.enums.LeadGrade;
import com.vyg.eis.CRM.domain.CRM.enums.LeadPriority;
import com.vyg.eis.CRM.domain.CRM.enums.LeadSource;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LeadUpdateRequest {

    // Identity
    private String companyName;
    private String industryType;
    private String subIndustry;
    private String companySize;

    // Location
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pinCode;
    private String country;
    private String googleMapsLink;

    // Contact
    private String primaryContactName;
    private String designation;
    private String mobile;
    private String alternateMobile;
    private String email;
    private String whatsappNumber;

    // Source
    private LeadSource leadSource;
    private String campaignName;

    // Classification
    private LeadGrade leadGrade;
    private LeadPriority leadPriority;

    // Assignment
    private String assignedTo;
    private String assignedManager;
    private String territory;

    // Financial
    private BigDecimal estimatedDealValue;
    private String expectedRevenueMonth;
    private String productInterested;

    // Tracking
    private LocalDate nextFollowUpDate;

    // Notes
    private String leadDescription;
    private String internalNotes;
    private String tags;
    private String competitorInfo;

    // Audit
    private String updatedBy;
}
