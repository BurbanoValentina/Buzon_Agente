package com.ucc.complaints.service;

import org.springframework.stereotype.Component;

@Component
public class PredictionPolicy {
    private static final long MINIMUM_COMPLAINTS = 5;

    public boolean eligibleForCategory(long count) {
        return count >= MINIMUM_COMPLAINTS;
    }

    public boolean eligibleForTotal(long count) {
        return count >= MINIMUM_COMPLAINTS;
    }
}
