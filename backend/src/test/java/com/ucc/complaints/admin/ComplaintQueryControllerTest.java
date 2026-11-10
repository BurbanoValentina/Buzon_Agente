package com.ucc.complaints.admin;

import com.ucc.complaints.controller.AdminComplaintController;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminComplaintController.class)
class ComplaintQueryControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ComplaintQueryService queryService;
    @MockBean ExcelExportService excelExportService;

    @Test
    void filtersAreAcceptedAndUnauthenticatedRequestsAreRejected() throws Exception {
        when(queryService.list(any())).thenReturn(List.of());
        mvc.perform(get("/api/v1/admin/complaints").param("category", "Laboratorio")
                .param("status", ComplaintStatus.PENDIENTE.name()))
            .andExpect(status().isUnauthorized());
    }
}
