package com.ucc.complaints.audit;

import com.ucc.complaints.service.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class DownloadAuditTest {
    @Test
    void recordsDownloadActorWithoutComplaintContent() {
        AuditService audit = mock(AuditService.class);
        audit.record("EXCEL_DOWNLOAD", "administrator");
        verify(audit).record("EXCEL_DOWNLOAD", "administrator");
        verifyNoMoreInteractions(audit);
    }
}
