package com.vyg.eis.CRM.service.CRM;

import com.vyg.eis.CRM.common.exception.DuplicateLeadException;
import com.vyg.eis.CRM.common.exception.ResourceNotFoundException;
import com.vyg.eis.CRM.domain.CRM.ActivityLog;
import com.vyg.eis.CRM.domain.CRM.Lead;
import com.vyg.eis.CRM.domain.CRM.enums.ActivityType;
import com.vyg.eis.CRM.domain.CRM.enums.LeadGrade;
import com.vyg.eis.CRM.domain.CRM.enums.LeadPriority;
import com.vyg.eis.CRM.domain.CRM.enums.LeadSource;
import com.vyg.eis.CRM.domain.CRM.enums.LeadStatus;
import com.vyg.eis.CRM.dto.ActivityLogRequest;
import com.vyg.eis.CRM.dto.LeadAssignRequest;
import com.vyg.eis.CRM.dto.LeadCreateRequest;
import com.vyg.eis.CRM.dto.LeadStatusUpdateRequest;
import com.vyg.eis.CRM.dto.LeadUpdateRequest;
import com.vyg.eis.CRM.repository.CRM.LeadRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final ActivityLogService activityLogService;
    private final LeadStatusTransitionValidator transitionValidator;

    @Transactional
    public Lead createLead(LeadCreateRequest request) {
        if (request.getMobile() != null && leadRepository.existsByMobileAndIsActiveTrue(request.getMobile())) {
            throw new DuplicateLeadException("Lead with mobile " + request.getMobile() + " already exists");
        }
        if (request.getEmail() != null && leadRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new DuplicateLeadException("Lead with email " + request.getEmail() + " already exists");
        }

        Lead lead = Lead.builder()
                .companyName(request.getCompanyName())
                .industryType(request.getIndustryType())
                .subIndustry(request.getSubIndustry())
                .companySize(request.getCompanySize())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .country(request.getCountry())
                .googleMapsLink(request.getGoogleMapsLink())
                .primaryContactName(request.getPrimaryContactName())
                .designation(request.getDesignation())
                .mobile(request.getMobile())
                .alternateMobile(request.getAlternateMobile())
                .email(request.getEmail())
                .whatsappNumber(request.getWhatsappNumber())
                .leadSource(request.getLeadSource() != null ? request.getLeadSource() : LeadSource.MANUAL)
                .campaignName(request.getCampaignName())
                .leadGrade(request.getLeadGrade())
                .leadStatus(request.getLeadStatus() != null ? request.getLeadStatus() : LeadStatus.NEW)
                .leadPriority(request.getLeadPriority())
                .assignedTo(request.getAssignedTo())
                .assignedManager(request.getAssignedManager())
                .territory(request.getTerritory())
                .estimatedDealValue(request.getEstimatedDealValue())
                .expectedRevenueMonth(request.getExpectedRevenueMonth())
                .productInterested(request.getProductInterested())
                .nextFollowUpDate(request.getNextFollowUpDate())
                .leadDescription(request.getLeadDescription())
                .internalNotes(request.getInternalNotes())
                .tags(request.getTags())
                .competitorInfo(request.getCompetitorInfo())
                .createdBy(request.getCreatedBy())
                .updatedBy(request.getCreatedBy())
                .isActive(true)
                .build();

        // First save to get the DB-generated id
        lead = leadRepository.save(lead);

        // Generate and assign the human-readable leadId
        lead.setLeadId(generateLeadId(lead.getId()));
        lead = leadRepository.save(lead);

        activityLogService.logActivity(lead, ActivityType.LEAD_CREATED,
                "Lead created: " + lead.getCompanyName(), request.getCreatedBy());

        return lead;
    }

    private String generateLeadId(Long dbId) {
        int year = LocalDate.now().getYear();
        String prefix = "LEAD-" + year + "-";
        Optional<String> lastId = leadRepository.findLastLeadIdByYearPrefix(prefix + "%");

        int nextSeq;
        if (lastId.isPresent()) {
            String last = lastId.get();
            String seqPart = last.substring(last.lastIndexOf('-') + 1);
            try {
                nextSeq = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        } else {
            nextSeq = 1;
        }

        return String.format("%s%05d", prefix, nextSeq);
    }

    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .filter(l -> Boolean.TRUE.equals(l.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
    }

    public Page<Lead> getLeads(LeadStatus status, LeadGrade grade, LeadPriority priority,
                                String city, String assignedTo, Pageable pageable) {
        Specification<Lead> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            if (status != null) predicates.add(cb.equal(root.get("leadStatus"), status));
            if (grade != null) predicates.add(cb.equal(root.get("leadGrade"), grade));
            if (priority != null) predicates.add(cb.equal(root.get("leadPriority"), priority));
            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (assignedTo != null && !assignedTo.isBlank()) {
                predicates.add(cb.equal(root.get("assignedTo"), assignedTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return leadRepository.findAll(spec, pageable);
    }

    @Transactional
    public Lead updateLead(Long id, LeadUpdateRequest request) {
        Lead lead = getLeadById(id);

        if (request.getMobile() != null && !request.getMobile().equals(lead.getMobile())
                && leadRepository.existsByMobileAndIsActiveTrueAndIdNot(request.getMobile(), id)) {
            throw new DuplicateLeadException("Lead with mobile " + request.getMobile() + " already exists");
        }
        if (request.getEmail() != null && !request.getEmail().equals(lead.getEmail())
                && leadRepository.existsByEmailAndIsActiveTrueAndIdNot(request.getEmail(), id)) {
            throw new DuplicateLeadException("Lead with email " + request.getEmail() + " already exists");
        }

        if (request.getCompanyName() != null) lead.setCompanyName(request.getCompanyName());
        if (request.getIndustryType() != null) lead.setIndustryType(request.getIndustryType());
        if (request.getSubIndustry() != null) lead.setSubIndustry(request.getSubIndustry());
        if (request.getCompanySize() != null) lead.setCompanySize(request.getCompanySize());
        if (request.getAddressLine1() != null) lead.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) lead.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) lead.setCity(request.getCity());
        if (request.getState() != null) lead.setState(request.getState());
        if (request.getPinCode() != null) lead.setPinCode(request.getPinCode());
        if (request.getCountry() != null) lead.setCountry(request.getCountry());
        if (request.getGoogleMapsLink() != null) lead.setGoogleMapsLink(request.getGoogleMapsLink());
        if (request.getPrimaryContactName() != null) lead.setPrimaryContactName(request.getPrimaryContactName());
        if (request.getDesignation() != null) lead.setDesignation(request.getDesignation());
        if (request.getMobile() != null) lead.setMobile(request.getMobile());
        if (request.getAlternateMobile() != null) lead.setAlternateMobile(request.getAlternateMobile());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getWhatsappNumber() != null) lead.setWhatsappNumber(request.getWhatsappNumber());
        if (request.getLeadSource() != null) lead.setLeadSource(request.getLeadSource());
        if (request.getCampaignName() != null) lead.setCampaignName(request.getCampaignName());
        if (request.getLeadGrade() != null) lead.setLeadGrade(request.getLeadGrade());
        if (request.getLeadPriority() != null) lead.setLeadPriority(request.getLeadPriority());
        if (request.getAssignedTo() != null) lead.setAssignedTo(request.getAssignedTo());
        if (request.getAssignedManager() != null) lead.setAssignedManager(request.getAssignedManager());
        if (request.getTerritory() != null) lead.setTerritory(request.getTerritory());
        if (request.getEstimatedDealValue() != null) lead.setEstimatedDealValue(request.getEstimatedDealValue());
        if (request.getExpectedRevenueMonth() != null) lead.setExpectedRevenueMonth(request.getExpectedRevenueMonth());
        if (request.getProductInterested() != null) lead.setProductInterested(request.getProductInterested());
        if (request.getNextFollowUpDate() != null) lead.setNextFollowUpDate(request.getNextFollowUpDate());
        if (request.getLeadDescription() != null) lead.setLeadDescription(request.getLeadDescription());
        if (request.getInternalNotes() != null) lead.setInternalNotes(request.getInternalNotes());
        if (request.getTags() != null) lead.setTags(request.getTags());
        if (request.getCompetitorInfo() != null) lead.setCompetitorInfo(request.getCompetitorInfo());
        if (request.getUpdatedBy() != null) lead.setUpdatedBy(request.getUpdatedBy());

        return leadRepository.save(lead);
    }

    @Transactional
    public void deleteLead(Long id, String deletedBy) {
        Lead lead = getLeadById(id);
        lead.setIsActive(false);
        lead.setUpdatedBy(deletedBy);
        leadRepository.save(lead);
    }

    @Transactional
    public Lead assignLead(Long id, LeadAssignRequest request) {
        Lead lead = getLeadById(id);
        if (request.getAssignedTo() != null) lead.setAssignedTo(request.getAssignedTo());
        if (request.getAssignedManager() != null) lead.setAssignedManager(request.getAssignedManager());
        if (request.getTerritory() != null) lead.setTerritory(request.getTerritory());
        if (request.getUpdatedBy() != null) lead.setUpdatedBy(request.getUpdatedBy());
        lead = leadRepository.save(lead);

        activityLogService.logActivity(lead, ActivityType.NOTE,
                "Lead assigned to " + request.getAssignedTo(), request.getUpdatedBy());

        return lead;
    }

    @Transactional
    public Lead updateStatus(Long id, LeadStatusUpdateRequest request) {
        Lead lead = getLeadById(id);
        LeadStatus oldStatus = lead.getLeadStatus();
        LeadStatus newStatus = request.getNewStatus();

        transitionValidator.validate(oldStatus, newStatus);

        lead.setLeadStatus(newStatus);
        lead.setLastActivityDate(LocalDate.now());
        lead.setDaysSinceLastContact(0);
        lead.setUpdatedBy(request.getPerformedBy());
        lead = leadRepository.save(lead);

        activityLogService.logStatusChange(lead, oldStatus, newStatus,
                request.getSummary(), request.getPerformedBy());

        return lead;
    }

    @Transactional
    public ActivityLog addActivity(Long leadId, ActivityLogRequest request) {
        Lead lead = getLeadById(leadId);
        lead.setLastActivityDate(LocalDate.now());
        lead.setDaysSinceLastContact(0);
        leadRepository.save(lead);
        return activityLogService.logFromRequest(lead, request);
    }
}
