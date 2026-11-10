package com.ucc.complaints.service;

public class ComplaintValidationException extends RuntimeException {
    public ComplaintValidationException(String message) {
        super(message);
    }
}
