package com.ucc.complaints.controller;

import com.ucc.complaints.dto.publiccomplaint.*;
import com.ucc.complaints.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicComplaintController {
    private final ComplaintService service;
    private final TelegramNotificationService notificationService;

    public PublicComplaintController(ComplaintService service,
                                     TelegramNotificationService notificationService) {
        this.service = service;
        this.notificationService = notificationService;
    }

    @GetMapping("/form-options")
    public FormOptionsResponse formOptions() {
        return new FormOptionsResponse(ComplaintCategories.VALUES,
            List.of("image/jpeg", "image/png"), 5, 100L * 1024 * 1024);
    }

    @PostMapping(value = "/complaints", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PublicComplaintResponse create(
        @RequestPart("complaint") PublicComplaintRequest request,
        @RequestPart(value = "evidence", required = false) List<MultipartFile> evidence) {
        PublicComplaintResponse response = service.create(request, evidence);
        notificationService.notifyNewComplaint(response.complaintId(), request.category());
        return response;
    }
}
