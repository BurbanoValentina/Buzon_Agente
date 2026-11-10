package com.ucc.complaints.complaint;

import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.repository.ComplaintRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ComplaintPersistenceIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    ComplaintRepository complaints;

    @Test
    void newComplaintUsesPendingStatusAndAnonymousIdentityIsEmpty() {
        assertEquals("postgres", postgres.getUsername());
        assertEquals(ComplaintStatus.PENDIENTE, ComplaintStatus.valueOf("PENDIENTE"));
        Complaint complaint = new Complaint("Q-IT-001", true, "Infraestructura",
            "Asunto válido", "Descripción suficientemente larga para persistencia",
            OffsetDateTime.now());
        complaint.setName(null);
        complaint.setSemester(null);

        Complaint saved = complaints.saveAndFlush(complaint);

        assertNotNull(saved.getId());
        assertEquals(ComplaintStatus.PENDIENTE, saved.getStatus());
        assertTrue(saved.isAnonymous());
        assertNull(saved.getName());
        assertNull(saved.getSemester());
    }
}
