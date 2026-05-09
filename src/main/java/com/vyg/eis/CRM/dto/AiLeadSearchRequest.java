package com.vyg.eis.CRM.dto;

import lombok.Data;

@Data
public class AiLeadSearchRequest {
    private String query;
    private Integer maxResults;
}
