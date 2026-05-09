package com.vyg.eis.CRM.service.CRM;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyg.eis.CRM.domain.CRM.Lead;
import com.vyg.eis.CRM.domain.CRM.enums.LeadSource;
import com.vyg.eis.CRM.dto.AiLeadBulkCreateRequest;
import com.vyg.eis.CRM.dto.AiLeadResult;
import com.vyg.eis.CRM.dto.AiLeadSearchRequest;
import com.vyg.eis.CRM.dto.LeadCreateRequest;
import com.vyg.eis.CRM.repository.CRM.LeadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AiLeadSearchService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String SYSTEM_PROMPT =
            "You are a B2B lead intelligence assistant. Parse the query, extract industry and location, " +
            "return a JSON array of matching Indian companies with fields: companyName, address, phone, email, " +
            "website, gstNumber, industryType, subIndustry, employeeSize, city, state, " +
            "confidenceScore (HIGH/MEDIUM/LOW). Return ONLY JSON array.";

    private final LeadRepository leadRepository;
    private final LeadService leadService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${nexcrm.ai.openai-api-key:}")
    private String openaiApiKey;

    public AiLeadSearchService(LeadRepository leadRepository,
                                LeadService leadService,
                                ObjectMapper objectMapper,
                                RestTemplateBuilder restTemplateBuilder) {
        this.leadRepository = leadRepository;
        this.leadService = leadService;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder.build();
    }

    public List<AiLeadResult> search(AiLeadSearchRequest request) {
        String rawContent = callOpenAI(request.getQuery(), request.getMaxResults());
        List<AiLeadResult> results = parseAiResponse(rawContent);
        enrichWithCrmStatus(results);
        return results;
    }

    public List<Lead> bulkCreate(AiLeadBulkCreateRequest request) {
        List<Lead> created = new ArrayList<>();
        if (request.getLeads() == null) return created;

        for (AiLeadResult aiLead : request.getLeads()) {
            if (aiLead.isExistsInCrm()) continue;

            LeadCreateRequest createRequest = new LeadCreateRequest();
            createRequest.setCompanyName(aiLead.getCompanyName());
            createRequest.setIndustryType(aiLead.getIndustryType());
            createRequest.setSubIndustry(aiLead.getSubIndustry());
            createRequest.setCity(aiLead.getCity());
            createRequest.setState(aiLead.getState());
            createRequest.setAddressLine1(aiLead.getAddress());
            createRequest.setMobile(aiLead.getPhone());
            createRequest.setEmail(aiLead.getEmail());
            createRequest.setLeadSource(LeadSource.AI_SEARCH);
            createRequest.setAssignedTo(request.getAssignedTo());
            createRequest.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system");

            try {
                created.add(leadService.createLead(createRequest));
            } catch (Exception e) {
                log.warn("Skipping AI lead '{}': {}", aiLead.getCompanyName(), e.getMessage());
            }
        }
        return created;
    }

    private String callOpenAI(String query, Integer maxResults) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key is not configured (nexcrm.ai.openai-api-key)");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        String userContent = maxResults != null
                ? query + " (return up to " + maxResults + " results)"
                : query;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", "gpt-4o");
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userContent)
        ));
        body.put("max_tokens", 4000);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    OPENAI_URL, HttpMethod.POST, entity, JsonNode.class);
            return response.getBody()
                    .path("choices").get(0)
                    .path("message").path("content")
                    .asText();
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            throw new RuntimeException("AI search failed: " + e.getMessage());
        }
    }

    private List<AiLeadResult> parseAiResponse(String content) {
        try {
            String cleaned = content.strip();
            // Strip markdown code fences if present
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("(?s)```[a-z]*\\n?", "").replace("```", "").strip();
            }
            return objectMapper.readValue(cleaned, new TypeReference<List<AiLeadResult>>() {});
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void enrichWithCrmStatus(List<AiLeadResult> results) {
        for (AiLeadResult result : results) {
            boolean exists = false;
            Long existingId = null;

            if (result.getCompanyName() != null && result.getCity() != null) {
                var match = leadRepository.findByCompanyNameIgnoreCaseAndCityIgnoreCaseAndIsActiveTrue(
                        result.getCompanyName(), result.getCity());
                if (match.isPresent()) {
                    exists = true;
                    existingId = match.get().getId();
                }
            }

            if (!exists && result.getPhone() != null) {
                var match = leadRepository.findByMobileAndIsActiveTrue(result.getPhone());
                if (match.isPresent()) {
                    exists = true;
                    existingId = match.get().getId();
                }
            }

            result.setExistsInCrm(exists);
            result.setExistingLeadId(existingId);
        }
    }
}
