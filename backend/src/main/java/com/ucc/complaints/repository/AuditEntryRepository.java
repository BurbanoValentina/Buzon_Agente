package com.ucc.complaints.repository;

import com.ucc.complaints.model.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> { }
