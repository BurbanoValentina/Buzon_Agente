package com.ucc.complaints.service;

import com.ucc.complaints.model.AuditEntry;
import com.ucc.complaints.repository.AuditEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
    private final AuditEntryRepository repository;

    public AuditService(AuditEntryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String eventType, String actor) {
        AuditEntry entry = new AuditEntry();
        entry.setEventType(eventType);
        entry.setActor(actor);
        entry.setOccurredAt(OffsetDateTime.now());
        entry.setMetadata("{}");
        repository.save(entry);
        LOGGER.info("audit_event type={} actor_present={}", eventType, actor != null && !actor.isBlank());
    }
}
