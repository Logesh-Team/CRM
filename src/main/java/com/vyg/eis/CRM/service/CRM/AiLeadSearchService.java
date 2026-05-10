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

    private static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_MODEL = "claude-sonnet-4-6";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private static final String SYSTEM_PROMPT =
            "You are a B2B lead intelligence assistant for the Indian market. " +
            "Parse the query, extract industry type and location, then return a JSON array of real " +
            "matching Indian companies. Each object must have exactly these fields: " +
            "companyName (string), address (string), phone (string), email (string), " +
            "website (string), gstNumber (string), industryType (string), subIndustry (string), " +
            "employeeSize (string), city (string), state (string), " +
            "confidenceScore (one of: HIGH, MEDIUM, LOW). " +
            "Return ONLY a valid JSON array with no markdown, no explanation, no code fences. " +
            "If a field is unknown, use an empty string. Do not return null values.";

    private final LeadRepository leadRepository;
    private final LeadService leadService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${nexcrm.ai.claude-api-key:}")
    private String claudeApiKey;

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
        String rawContent = callClaude(request.getQuery(), request.getMaxResults());
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

    private String callClaude(String query, Integer maxResults) {
        if (claudeApiKey == null || claudeApiKey.isBlank()) {
            throw new RuntimeException("Claude API key is not configured (nexcrm.ai.claude-api-key)");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", claudeApiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);

        String userContent = maxResults != null
                ? query + ". Return up to " + maxResults + " companies."
                : query;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", CLAUDE_MODEL);
        body.put("max_tokens", 4096);
        body.put("system", SYSTEM_PROMPT);
        body.put("messages", List.of(
                Map.of("role", "user", "content", userContent)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    CLAUDE_URL, HttpMethod.POST, entity, JsonNode.class);
            JsonNode responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from Claude API");
            }
            // Claude response: { "content": [ { "type": "text", "text": "..." } ] }
            return responseBody.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Claude API call failed: {}", e.getMessage());
            throw new RuntimeException("AI search failed: " + e.getMessage());
        }
    }

    private List<AiLeadResult> parseAiResponse(String content) {
        try {
            String cleaned = content.strip();
            // Strip markdown code fences if Claude wraps with ```json ... ```
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("(?s)```[a-z]*\\n?", "").replace("```", "").strip();
            }
            return objectMapper.readValue(cleaned, new TypeReference<List<AiLeadResult>>() {});
        } catch (Exception e) {
            log.error("Failed to parse Claude response: {}\nRaw: {}", e.getMessage(), content);
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

            if (!exists && result.getPhone() != null && !result.getPhone().isBlank()) {
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
