package com.ucc.complaints.storage;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

public class GoogleDriveEvidenceStorage {
    public interface Client {
        String upload(String folderName, String fileName, String mimeType, InputStream content) throws IOException;
    }

    private final Client client;
    private final ImageMetadataSanitizer sanitizer;

    public GoogleDriveEvidenceStorage(Client client, ImageMetadataSanitizer sanitizer) {
        this.client = client;
        this.sanitizer = sanitizer;
    }

    public String store(String complaintId, LocalDate registrationDate, String category,
                        String fileName, String mimeType, InputStream content) throws IOException {
        String folder = complaintId + "-" + registrationDate + "-" + category;
        return client.upload(folder, fileName, mimeType,
            new java.io.ByteArrayInputStream(sanitizer.sanitize(content)));
    }
}
