package com.ucc.complaints.service;

import com.ucc.complaints.dto.publiccomplaint.*;
import com.ucc.complaints.model.Complaint;
import com.ucc.complaints.model.Evidence;
import com.ucc.complaints.repository.ComplaintRepository;
import com.ucc.complaints.repository.EvidenceRepository;
import com.ucc.complaints.repository.StatusHistoryRepository;
import com.ucc.complaints.storage.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ComplaintService {
    private static final long MAX_FILE_BYTES = 100L * 1024 * 1024;
    private final ComplaintRepository repository;
    private final StatusHistoryRepository statusHistories;
    private final EvidenceRepository evidenceRepository;
    private final SupabaseStorageService storageService;

    public ComplaintService(ComplaintRepository repository) {
        this(repository, null, null, null);
    }

    public ComplaintService(ComplaintRepository repository, StatusHistoryRepository statusHistories) {
        this(repository, statusHistories, null, null);
    }

    @Autowired
    public ComplaintService(ComplaintRepository repository,
                            StatusHistoryRepository statusHistories,
                            EvidenceRepository evidenceRepository,
                            SupabaseStorageService storageService) {
        this.repository = repository;
        this.statusHistories = statusHistories;
        this.evidenceRepository = evidenceRepository;
        this.storageService = storageService;
    }

    public static ComplaintService forValidationOnly() {
        return new ComplaintService(null);
    }

    public void validateEvidence(String mimeType, int count, long bytes) {
        if (!Set.of("image/jpeg", "image/png").contains(mimeType)) {
            throw new ComplaintValidationException("Solo se permiten imágenes JPG, JPEG o PNG.");
        }
        if (count < 1 || count > 5) {
            throw new ComplaintValidationException("Puedes adjuntar máximo 5 imágenes.");
        }
        if (bytes < 0 || bytes > MAX_FILE_BYTES) {
            throw new ComplaintValidationException("Cada imagen puede pesar máximo 100 MB.");
        }
    }

    public PublicComplaintResponse create(PublicComplaintRequest request, List<MultipartFile> files) {
        validate(request, files);
        boolean anonymous = "anonymous".equals(request.userType());
        String id = UUID.randomUUID().toString();
        Complaint complaint = new Complaint(id, anonymous, request.category(), request.subject().trim(),
            request.description().trim(), OffsetDateTime.now());
        if (!blank(request.locationNumber())) {
            complaint.setLocationNumber(request.locationNumber().trim());
        }
        if (!anonymous) {
            complaint.setName(request.name().trim());
            complaint.setSemester(request.semester().trim());
        }
        repository.save(complaint);

        if (files != null && !files.isEmpty() && storageService != null && evidenceRepository != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String publicUrl = storageService.uploadEvidence(id, file);
                    if (publicUrl != null) {
                        evidenceRepository.save(new Evidence(complaint, file.getOriginalFilename(),
                            file.getContentType(), file.getSize(), publicUrl));
                    }
                }
            }
        }

        return new PublicComplaintResponse(id, "Pendiente", "La queja fue recibida correctamente.");
    }

    private void validate(PublicComplaintRequest request, List<MultipartFile> files) {
        if (request == null || (!"anonymous".equals(request.userType()) && !"identified".equals(request.userType()))) {
            throw new ComplaintValidationException("Selecciona el tipo de usuario.");
        }
        if ("identified".equals(request.userType())
            && (blank(request.name()) || blank(request.semester()))) {
            throw new ComplaintValidationException("Nombre y semestre son obligatorios.");
        }
        if (!ComplaintCategories.VALUES.contains(request.category())) {
            throw new ComplaintValidationException("Selecciona una categoría válida.");
        }
        boolean needsNumber = Set.of("Laboratorio", "Aulas").contains(request.category());
        if (needsNumber && blank(request.locationNumber())) {
            throw new ComplaintValidationException("El número es obligatorio para esta categoría.");
        }
        if (request.locationNumber() != null && request.locationNumber().length() > 50) {
            throw new ComplaintValidationException("El número no puede superar 50 caracteres.");
        }
        if (request.subject() == null || request.subject().length() < 5 || request.subject().length() > 200) {
            throw new ComplaintValidationException("El asunto debe tener entre 5 y 200 caracteres.");
        }
        if (request.description() == null || request.description().length() < 20 || request.description().length() > 500) {
            throw new ComplaintValidationException("La descripción debe tener entre 20 y 500 caracteres.");
        }
        if (!request.privacyAccepted()) {
            throw new ComplaintValidationException("Debes aceptar el aviso de privacidad.");
        }
        if (files != null) {
            if (files.size() > 5) {
                throw new ComplaintValidationException("Puedes adjuntar máximo 5 imágenes.");
            }
            files.forEach(file -> validateEvidence(file.getContentType(), 1, file.getSize()));
        }
    }

    public PublicComplaintStatusResponse getStatus(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new ComplaintValidationException("El código de radicado es obligatorio.");
        }
        Complaint complaint = repository.findByPublicId(publicId.trim())
            .orElseThrow(() -> new ComplaintValidationException("No se encontró ninguna queja con el radicado proporcionado."));

        List<PublicStatusHistoryItem> historyItems = statusHistories == null ? List.of() :
            statusHistories.findByComplaintIdOrderByChangedAtAsc(complaint.getId())
                .stream()
                .map(h -> new PublicStatusHistoryItem(
                    h.getPreviousStatus(),
                    h.getNewStatus(),
                    h.getComment(),
                    h.getChangedAt()))
                .toList();

        return new PublicComplaintStatusResponse(
            complaint.getPublicId(),
            complaint.getCategory(),
            complaint.getStatus().name(),
            complaint.getSubject(),
            complaint.getRegisteredAt(),
            complaint.getCompletedAt(),
            historyItems
        );
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
