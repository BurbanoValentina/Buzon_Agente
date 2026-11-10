package com.ucc.complaints.controller;

import com.ucc.complaints.dto.dashboard.DashboardResponse;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void getsAggregates() throws Exception {
        DashboardResponse response = new DashboardResponse(
            0L,
            Map.of(),
            Map.of(ComplaintStatus.PENDIENTE, 0L),
            List.of(),
            0L,
            0L,
            List.of(),
            List.of(),
            List.of()
        );
        when(dashboardService.getDashboard(any(), any(), any())).thenReturn(response);

        mvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isOk());
    }
}
