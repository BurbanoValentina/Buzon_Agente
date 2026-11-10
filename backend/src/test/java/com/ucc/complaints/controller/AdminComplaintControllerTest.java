package com.ucc.complaints.controller;

import com.ucc.complaints.dto.admin.ComplaintDetail;
import com.ucc.complaints.dto.admin.ComplaintSummary;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.service.ComplaintQueryService;
import com.ucc.complaints.service.ExcelExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminComplaintController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminComplaintControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ComplaintQueryService queryService;

    @MockBean
    private ExcelExportService excelExportService;

    @Test
    void listsAndDetailsAndExportsComplaints() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        ComplaintSummary summary = new ComplaintSummary("C-1", now, "Infraestructura", "Lab 1", false, ComplaintStatus.PENDIENTE);
        ComplaintDetail detail = new ComplaintDetail("C-1", now, "Infraestructura", "Lab 1", false, "Nombre", "5", "Asunto", "Desc", ComplaintStatus.PENDIENTE, null, List.of());

        when(queryService.list(any())).thenReturn(List.of(summary));
        when(queryService.detail("C-1")).thenReturn(detail);
        when(excelExportService.export(any(), any())).thenReturn(new byte[]{1, 2, 3});

        mvc.perform(get("/api/v1/admin/complaints")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/complaints/C-1")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/complaints/export.xlsx")).andExpect(status().isOk());
    }
}
