package com.ucc.complaints.controller;

import com.ucc.complaints.dto.report.WordReportDraftResponse;
import com.ucc.complaints.service.WordReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WordReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class WordReportControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private WordReportService service;

    @Test
    void testDraftLifecycleAndDownload() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        WordReportDraftResponse draft = new WordReportDraftResponse(1L, "Titulo", "Contenido", now, now);
        when(service.create(any(), any())).thenReturn(draft);
        when(service.update(any(), any(), any(), any())).thenReturn(draft);
        when(service.get(1L)).thenReturn(draft);
        when(service.generateWord(any(), any())).thenReturn(new byte[]{1, 2, 3});

        mvc.perform(post("/api/v1/admin/reports/word/drafts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"complaintIds\":[\"C-1\"],\"category\":\"Infraestructura\"}"))
            .andExpect(status().isOk());

        mvc.perform(put("/api/v1/admin/reports/word/drafts/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Nuevo Titulo\",\"content\":\"Nuevo Contenido\"}"))
            .andExpect(status().isOk());

        mvc.perform(get("/api/v1/admin/reports/word/drafts/1")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/admin/reports/word/drafts/1/download")).andExpect(status().isOk());
    }
}
