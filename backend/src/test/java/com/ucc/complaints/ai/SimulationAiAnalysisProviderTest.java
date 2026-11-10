package com.ucc.complaints.ai;

import com.ucc.complaints.dto.ai.AiPredictionInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationAiAnalysisProviderTest {
    private final SimulationAiAnalysisProvider provider = new SimulationAiAnalysisProvider();

    @Test
    void estimatesNextWeekAndMonth() {
        AiPredictionInput input = new AiPredictionInput("Infraestructura", 5, 20);
        String week = provider.estimateNextWeek(input);
        String month = provider.estimateNextMonth(input);

        assertTrue(week.contains("semana"));
        assertTrue(month.contains("mes"));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> provider.estimateNextWeek(null));
        assertThrows(IllegalArgumentException.class, () -> provider.estimateNextWeek(new AiPredictionInput("", 5, 20)));
        assertThrows(IllegalArgumentException.class, () -> provider.estimateNextWeek(new AiPredictionInput("Cat", -1, 20)));
        assertThrows(IllegalArgumentException.class, () -> provider.estimateNextWeek(new AiPredictionInput("Cat", 5, -1)));
    }
}
