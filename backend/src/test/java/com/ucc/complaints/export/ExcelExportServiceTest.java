package com.ucc.complaints.export;

import com.ucc.complaints.dto.admin.ComplaintFilter;
import com.ucc.complaints.service.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExcelExportServiceTest {
    @Test
    void createsXlsxWithDefinedColumnsAndAuditsDownload() throws Exception {
        ComplaintQueryService query = mock(ComplaintQueryService.class);
        AuditService audit = mock(AuditService.class);
        when(query.matching(any())).thenReturn(List.of());
        byte[] result = new ExcelExportService(query, audit).export(new ComplaintFilter(null, null, null, null, null), "admin");
        assertTrue(result.length > 0);
        verify(audit).record("EXCEL_DOWNLOAD", "admin");
    }
}
