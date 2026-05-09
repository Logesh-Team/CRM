package com.vyg.eis.CRM.controller.CRM;

import com.vyg.eis.CRM.common.ApiResponse;
import com.vyg.eis.CRM.domain.CRM.ActivityLog;
import com.vyg.eis.CRM.domain.CRM.Lead;
import com.vyg.eis.CRM.domain.CRM.enums.LeadGrade;
import com.vyg.eis.CRM.domain.CRM.enums.LeadPriority;
import com.vyg.eis.CRM.domain.CRM.enums.LeadStatus;
import com.vyg.eis.CRM.dto.ActivityLogRequest;
import com.vyg.eis.CRM.dto.LeadAssignRequest;
import com.vyg.eis.CRM.dto.LeadCreateRequest;
import com.vyg.eis.CRM.dto.LeadStatusUpdateRequest;
import com.vyg.eis.CRM.dto.LeadUpdateRequest;
import com.vyg.eis.CRM.service.CRM.ActivityLogService;
import com.vyg.eis.CRM.service.CRM.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<Lead>> createLead(@RequestBody LeadCreateRequest request) {
        Lead lead = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lead, "Lead created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Lead>>> getLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadGrade grade,
            @RequestParam(required = false) LeadPriority priority,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String assignedTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Lead> leads = leadService.getLeads(status, grade, priority, city, assignedTo, pageable);
        return ResponseEntity.ok(ApiResponse.success(leads, "Leads fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Lead>> getLead(@PathVariable Long id) {
        Lead lead = leadService.getLeadById(id);
        return ResponseEntity.ok(ApiResponse.success(lead, "Lead fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Lead>> updateLead(@PathVariable Long id,
                                                         @RequestBody LeadUpdateRequest request) {
        Lead lead = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.success(lead, "Lead updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLead(
            @PathVariable Long id,
            @RequestParam(defaultValue = "system") String deletedBy) {
        leadService.deleteLead(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Lead deleted successfully"));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Lead>> assignLead(@PathVariable Long id,
                                                         @RequestBody LeadAssignRequest request) {
        Lead lead = leadService.assignLead(id, request);
        return ResponseEntity.ok(ApiResponse.success(lead, "Lead assigned successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Lead>> updateStatus(@PathVariable Long id,
                                                           @RequestBody LeadStatusUpdateRequest request) {
        Lead lead = leadService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(lead, "Lead status updated successfully"));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<ActivityLog>> addActivity(@PathVariable Long id,
                                                                 @RequestBody ActivityLogRequest request) {
        ActivityLog log = leadService.addActivity(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(log, "Activity logged successfully"));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<List<ActivityLog>>> getActivities(@PathVariable Long id) {
        leadService.getLeadById(id); // validate existence
        List<ActivityLog> logs = activityLogService.getLeadActivities(id);
        return ResponseEntity.ok(ApiResponse.success(logs, "Activities fetched successfully"));
    }
}
