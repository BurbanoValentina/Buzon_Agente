package com.ucc.complaints.evidence;

import com.ucc.complaints.service.ComplaintService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvidenceValidationTest {
    @Test
    void rejectsUnsupportedEvidenceLimits() {
        ComplaintService service = ComplaintService.forValidationOnly();
        assertThrows(RuntimeException.class, () -> service.validateEvidence("image/gif", 100, 1));
        assertThrows(RuntimeException.class, () -> service.validateEvidence("image/jpeg", 6, 1));
        assertThrows(RuntimeException.class, () -> service.validateEvidence("image/jpeg", 1, 100L * 1024 * 1024 + 1));
    }
}
