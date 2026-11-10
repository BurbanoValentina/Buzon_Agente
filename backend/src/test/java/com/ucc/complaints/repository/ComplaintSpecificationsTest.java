package com.ucc.complaints.repository;

import com.ucc.complaints.dto.admin.ComplaintFilter;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintSpecificationsTest {
    @Test
    void matchesFiltersCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();
        Complaint c = new Complaint("C-1", true, "Infraestructura", "Asunto", "Desc", now);
        c.setStatus(ComplaintStatus.PENDIENTE);

        assertTrue(ComplaintSpecifications.matches(null).test(c));

        ComplaintFilter f1 = new ComplaintFilter(now.minusDays(1), now.plusDays(1), "Infraestructura", ComplaintStatus.PENDIENTE, true);
        assertTrue(ComplaintSpecifications.matches(f1).test(c));

        ComplaintFilter f2 = new ComplaintFilter(null, null, "Bienestar", null, null);
        assertFalse(ComplaintSpecifications.matches(f2).test(c));

        ComplaintFilter f3 = new ComplaintFilter(null, null, null, ComplaintStatus.COMPLETADA, null);
        assertFalse(ComplaintSpecifications.matches(f3).test(c));

        ComplaintFilter f4 = new ComplaintFilter(null, null, null, null, false);
        assertFalse(ComplaintSpecifications.matches(f4).test(c));

        ComplaintFilter f5 = new ComplaintFilter(now.plusDays(1), null, null, null, null);
        assertFalse(ComplaintSpecifications.matches(f5).test(c));

        ComplaintFilter f6 = new ComplaintFilter(null, now.minusDays(1), null, null, null);
        assertFalse(ComplaintSpecifications.matches(f6).test(c));
    }
}
