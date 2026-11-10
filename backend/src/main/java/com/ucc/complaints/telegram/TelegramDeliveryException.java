package com.ucc.complaints.telegram;

public class TelegramDeliveryException extends RuntimeException {
    public TelegramDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
