package com.ucc.complaints.foundation;

import com.ucc.complaints.exception.ApiError;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiErrorTest {
    @Test
    void exposesUniformErrorFields() {
        ApiError error = new ApiError(Instant.now(), 400, "INVALID_REQUEST", "Datos inválidos.");
        assertEquals(400, error.status());
        assertEquals("INVALID_REQUEST", error.code());
    }
}
