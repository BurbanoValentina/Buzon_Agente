package com.ucc.complaints.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GoogleDriveEvidenceStorageTest {
    @Test
    void storesSanitizedEvidence() throws Exception {
        GoogleDriveEvidenceStorage.Client client = mock(GoogleDriveEvidenceStorage.Client.class);
        ImageMetadataSanitizer sanitizer = mock(ImageMetadataSanitizer.class);

        when(sanitizer.sanitize(any(InputStream.class))).thenReturn(new byte[]{10, 20});
        when(client.upload(eq("C-1-2026-03-01-Infraestructura"), eq("foto.png"), eq("image/png"), any(InputStream.class)))
            .thenReturn("drive-file-id-999");

        GoogleDriveEvidenceStorage storage = new GoogleDriveEvidenceStorage(client, sanitizer);
        String id = storage.store("C-1", LocalDate.of(2026, 3, 1), "Infraestructura", "foto.png", "image/png",
            new ByteArrayInputStream(new byte[]{1, 2, 3}));

        assertEquals("drive-file-id-999", id);
    }
}
