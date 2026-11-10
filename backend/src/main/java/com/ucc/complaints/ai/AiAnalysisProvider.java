package com.ucc.complaints.ai;

import com.ucc.complaints.dto.ai.AiPredictionInput;

public interface AiAnalysisProvider {
    String estimateNextWeek(AiPredictionInput input);

    String estimateNextMonth(AiPredictionInput input);
}
