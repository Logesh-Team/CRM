package com.vyg.eis.CRM.service.CRM;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyg.eis.CRM.domain.CRM.AuditLog;
import com.vyg.eis.CRM.repository.CRM.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(UUID actorId, String actorName, String action,
                    String entityType, String entityId,
                    Object oldValue, Object newValue, String ipAddress) {
        AuditLog entry = AuditLog.builder()
                .actorId(actorId)
                .actorName(actorName)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(toJson(oldValue))
                .newValue(toJson(newValue))
                .ipAddress(ipAddress)
                .performedAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(entry);
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit value: {}", e.getMessage());
            return value.toString();
        }
    }
}
