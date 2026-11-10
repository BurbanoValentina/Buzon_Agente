package com.ucc.complaints.ai;

import com.ucc.complaints.dto.ai.AiPredictionInput;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SimulationAiAnalysisProvider implements AiAnalysisProvider {
    @Override
    public String estimateNextWeek(AiPredictionInput input) {
        return validateInput(input) + " para la próxima semana";
    }

    @Override
    public String estimateNextMonth(AiPredictionInput input) {
        return validateInput(input) + " para el próximo mes";
    }

    private String validateInput(AiPredictionInput input) {
        if (input == null || input.category() == null || input.category().isBlank()) {
            throw new IllegalArgumentException("La entrada de predicción no contiene categoría válida");
        }
        if (input.categoryCount() < 0 || input.total() < 0) {
            throw new IllegalArgumentException("Las cantidades de predicción deben ser positivas");
        }
        return "Estimación aproximada basada únicamente en datos almacenados";
    }
}
