package com.ucc.complaints.status;

import com.ucc.complaints.controller.StatusController;
import com.ucc.complaints.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.ucc.complaints.security.SecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatusController.class)
@Import(SecurityConfig.class)
class StatusControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ComplaintStatusService statusService;
    @MockBean MonthlyFollowUpService followUpService;

    @Test
    void rejectsUnauthenticatedStatusChanges() throws Exception {
        mvc.perform(patch("/api/v1/admin/complaints/C-1/status")
            .contentType("application/json")
            .content("{\"status\":\"COMPLETADA\",\"comment\":\"Atendida\"}"))
            .andExpect(status().isForbidden());
    }
}