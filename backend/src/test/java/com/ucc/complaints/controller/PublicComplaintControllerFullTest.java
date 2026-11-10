package com.ucc.complaints.controller;

import com.ucc.complaints.dto.publiccomplaint.PublicComplaintResponse;
import com.ucc.complaints.service.ComplaintService;
import com.ucc.complaints.service.TelegramNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicComplaintController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicComplaintControllerFullTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ComplaintService service;

    @MockBean
    private TelegramNotificationService notificationService;

    @Test
    void submitsComplaintSuccessfully() throws Exception {
        PublicComplaintResponse response = new PublicComplaintResponse("C-123", "Pendiente", "Recibida");
        when(service.create(any(), any())).thenReturn(response);

        MockMultipartFile complaintPart = new MockMultipartFile(
            "complaint",
            "",
            "application/json",
            "{\"userType\":\"anonymous\",\"category\":\"Infraestructura\",\"subject\":\"Asunto\",\"description\":\"Descripcion\",\"privacyAccepted\":true}".getBytes()
        );

        mvc.perform(multipart("/api/v1/public/complaints").file(complaintPart))
            .andExpect(status().isOk());

        verify(notificationService).notifyNewComplaint("C-123", "Infraestructura");
    }
}
