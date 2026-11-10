package com.ucc.complaints.publicapi;

import com.ucc.complaints.controller.PublicComplaintController;
import com.ucc.complaints.service.ComplaintService;
import com.ucc.complaints.service.TelegramNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicComplaintController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicComplaintControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ComplaintService service;
    @MockBean TelegramNotificationService notificationService;

    @Test
    void exposesPublicFormOptionsWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/v1/public/form-options")).andExpect(status().isOk());
    }
}
