package com.ucc.complaints.ai;

import com.ucc.complaints.dto.ai.AiPredictionInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiPrivacyBoundaryTest {
    @Test
    void aiInputContainsOnlyAggregatesAndCategory() {
        AiPredictionInput input = new AiPredictionInput("Aulas", 5, 5);
        String serialized = input.toString();
        assertTrue(serialized.contains("Aulas"));
        assertTrue(serialized.contains("5"));
        assertFalse(serialized.toLowerCase().contains("nombre"));
        assertFalse(serialized.toLowerCase().contains("semestre"));
        assertFalse(serialized.toLowerCase().contains("imagen"));
        assertFalse(serialized.toLowerCase().contains("descripcion"));
    }
}
