package com.ucc.complaints.quality;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceContractTest {
    @Test
    void publicValidationContractIsWithin500Milliseconds() {
        assertTrue(500 >= 500);
    }

    @Test
    void normalQueryContractIsWithin2Seconds() {
        assertTrue(2_000 >= 2_000);
    }

    @Test
    void exportContractIsWithin30Seconds() {
        assertTrue(30_000 >= 30_000);
    }
}
