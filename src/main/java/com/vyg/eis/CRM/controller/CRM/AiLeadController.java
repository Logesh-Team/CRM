package com.vyg.eis.CRM.controller.CRM;

import com.vyg.eis.CRM.common.ApiResponse;
import com.vyg.eis.CRM.domain.CRM.Lead;
import com.vyg.eis.CRM.dto.AiLeadBulkCreateRequest;
import com.vyg.eis.CRM.dto.AiLeadResult;
import com.vyg.eis.CRM.dto.AiLeadSearchRequest;
import com.vyg.eis.CRM.service.CRM.AiLeadSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/leads")
@RequiredArgsConstructor
public class AiLeadController {

    private final AiLeadSearchService aiLeadSearchService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<AiLeadResult>>> search(@RequestBody AiLeadSearchRequest request) {
        List<AiLeadResult> results = aiLeadSearchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(results, "AI search completed with " + results.size() + " results"));
    }

    @PostMapping("/bulk-create")
    public ResponseEntity<ApiResponse<List<Lead>>> bulkCreate(@RequestBody AiLeadBulkCreateRequest request) {
        List<Lead> leads = aiLeadSearchService.bulkCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(leads, leads.size() + " leads created from AI search"));
    }
}
