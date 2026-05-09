package com.vyg.eis.CRM.repository.CRM;

import com.vyg.eis.CRM.domain.CRM.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByLeadIdOrderByPerformedAtDesc(Long leadId);
}
