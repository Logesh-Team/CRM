package com.vyg.eis.CRM.repository.CRM;

import com.vyg.eis.CRM.domain.CRM.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByLeadIdAndIsActiveTrue(String leadId);

    boolean existsByMobileAndIsActiveTrue(String mobile);

    boolean existsByEmailAndIsActiveTrue(String email);

    boolean existsByMobileAndIsActiveTrueAndIdNot(String mobile, Long id);

    boolean existsByEmailAndIsActiveTrueAndIdNot(String email, Long id);

    // Returns the last lead_id for a given year prefix (e.g. "LEAD-2025-%")
    @Query(value = "SELECT lead_id FROM leads WHERE lead_id LIKE :prefix ORDER BY lead_id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastLeadIdByYearPrefix(@Param("prefix") String prefix);

    Optional<Lead> findByCompanyNameIgnoreCaseAndCityIgnoreCaseAndIsActiveTrue(String companyName, String city);

    Optional<Lead> findByMobileAndIsActiveTrue(String mobile);
}
