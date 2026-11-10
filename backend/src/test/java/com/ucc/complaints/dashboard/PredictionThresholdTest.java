package com.ucc.complaints.dashboard;

import com.ucc.complaints.service.PredictionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PredictionThresholdTest {
    @Test
    void requiresAtLeastFiveComplaints() {
        PredictionPolicy policy = new PredictionPolicy();
        assertFalse(policy.eligibleForCategory(4));
        assertTrue(policy.eligibleForCategory(5));
        assertFalse(policy.eligibleForTotal(4));
        assertTrue(policy.eligibleForTotal(5));
    }

    @Test
    void predictionsRemainClearlySeparatedAsEstimates() {
        PredictionPolicy policy = new PredictionPolicy();
        assertFalse(policy.eligibleForCategory(0));
        assertTrue(policy.eligibleForCategory(12));
    }
}
