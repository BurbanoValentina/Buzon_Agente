package com.ucc.complaints.dashboard;

import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.ComplaintStatus;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.service.DashboardService;
import com.ucc.complaints.service.PredictionPolicy;
import com.ucc.complaints.ai.AiAnalysisProvider;
import com.ucc.complaints.dto.dashboard.DashboardResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock ComplaintRepository complaints;
    @Mock PredictionPolicy predictionPolicy;
    @Mock AiAnalysisProvider aiAnalysisProvider;
    @InjectMocks DashboardService service;

    @Test
    void aggregatesStoredComplaintsAndEmptyMonths() {
        OffsetDateTime first = OffsetDateTime.parse("2026-01-10T10:00:00Z");
        OffsetDateTime second = OffsetDateTime.parse("2026-03-20T10:00:00Z");
        Complaint january = new Complaint("C-1", true, "Aulas", "Asunto uno", "Descripcion suficientemente larga uno", first);
        Complaint march = new Complaint("C-2", false, "Aulas", "Asunto dos", "Descripcion suficientemente larga dos", second);
        january.setStatus(ComplaintStatus.COMPLETADA);
        march.setStatus(ComplaintStatus.PENDIENTE);
        march.setName("Administrador privado");
        march.setSemester("8");
        when(complaints.findAll()).thenReturn(List.of(january, march));

        DashboardResponse response = service.getDashboard("MONTHLY", first.withYear(2026).withMonth(1), second.withYear(2026).withMonth(3));

        assertEquals(2, response.total());
        assertEquals(2L, response.byCategory().get("Aulas"));
        assertEquals(1L, response.byStatus().get(ComplaintStatus.COMPLETADA));
        assertEquals(1L, response.anonymous());
        assertEquals(1L, response.identified());
        assertEquals(3, response.monthly().size());
        assertNotNull(response.monthly().stream().filter(point -> point.month().endsWith("-02")).findFirst().orElse(null));
        assertEquals(0L, response.monthly().stream().filter(point -> point.month().endsWith("-02")).findFirst().orElseThrow().count());
    }
}
