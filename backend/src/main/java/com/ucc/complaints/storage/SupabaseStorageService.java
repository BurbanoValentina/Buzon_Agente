package com.ucc.complaints.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class SupabaseStorageService {
    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);
    private static final String BUCKET = "evidencias";

    @Value("${supabase.url:${SUPABASE_URL:}}")
    private String supabaseUrl;

    @Value("${supabase.secret-key:${SUPABASE_SECRET_KEY:}}")
    private String supabaseSecretKey;

    private final ImageMetadataSanitizer sanitizer;
    private final RestTemplate restTemplate;

    public SupabaseStorageService(ImageMetadataSanitizer sanitizer) {
        this.sanitizer = sanitizer;
        this.restTemplate = new RestTemplate();
    }

    public String uploadEvidence(String complaintPublicId, MultipartFile file) {
        if (supabaseUrl == null || supabaseUrl.isBlank() || supabaseSecretKey == null || supabaseSecretKey.isBlank()) {
            log.warn("Supabase credentials not configured for storage upload.");
            return null;
        }

        try {
            String rawName = file != null ? file.getOriginalFilename() : null;
            String originalName = (rawName == null || rawName.isBlank()) ? "evidencia.png" : rawName;
            String cleanFileName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storagePath = complaintPublicId + "/" + UUID.randomUUID().toString().substring(0, 8) + "_" + cleanFileName;
            String encodedPath = URLEncoder.encode(storagePath, StandardCharsets.UTF_8).replace("+", "%20");

            byte[] uploadBytes;
            String rawType = file != null ? file.getContentType() : null;
            String contentType = (rawType != null && !rawType.isBlank()) ? rawType : "image/png";

            if (file != null) {
                try (InputStream in = file.getInputStream()) {
                    if (sanitizer != null) {
                        uploadBytes = sanitizer.sanitize(in);
                        contentType = "image/png";
                    } else {
                        uploadBytes = in.readAllBytes();
                    }
                } catch (Exception sanitizeError) {
                    log.warn("Could not sanitize image metadata, proceeding with raw bytes: {}", sanitizeError.getMessage());
                    uploadBytes = file.getBytes();
                }
            } else {
                return null;
            }

            String baseUrl = supabaseUrl.replaceAll("/+$", "");
            String uploadUrl = baseUrl + "/storage/v1/object/" + BUCKET + "/" + encodedPath;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseSecretKey);
            headers.set("apiKey", supabaseSecretKey);
            headers.setContentType(MediaType.parseMediaType(contentType));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(uploadBytes, headers);
            ResponseEntity<String> response = restTemplate.exchange(URI.create(uploadUrl), HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = baseUrl + "/storage/v1/object/public/" + BUCKET + "/" + storagePath;
                log.info("Uploaded evidence to Supabase Storage: {}", publicUrl);
                return publicUrl;
            } else {
                log.error("Failed to upload evidence to Supabase Storage. Status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error uploading evidence to Supabase Storage: {}", e.getMessage(), e);
            return null;
        }
    }
}
