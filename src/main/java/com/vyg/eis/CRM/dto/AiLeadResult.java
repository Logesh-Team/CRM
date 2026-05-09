package com.vyg.eis.CRM.dto;

import lombok.Data;

@Data
public class AiLeadResult {
    private String companyName;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String gstNumber;
    private String industryType;
    private String subIndustry;
    private String employeeSize;
    private String city;
    private String state;
    private String confidenceScore;

    // Populated after CRM lookup
    private boolean existsInCrm;
    private Long existingLeadId;
}
