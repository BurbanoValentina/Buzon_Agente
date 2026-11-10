package com.ucc.complaints.controller;

import com.ucc.complaints.dto.status.*;
import com.ucc.complaints.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class StatusController {
    private final ComplaintStatusService statusService;
    private final MonthlyFollowUpService followUpService;

    public StatusController(ComplaintStatusService statusService,
                            MonthlyFollowUpService followUpService) {
        this.statusService = statusService;
        this.followUpService = followUpService;
    }

    @PatchMapping("/complaints/{id}/status")
    public List<StatusHistoryResponse> updateStatus(
        @PathVariable String id,
        @Valid @RequestBody StatusTransitionRequest request,
        @RequestHeader(value = "X-Admin-User", defaultValue = "administrator") String actor) {
        return statusService.transition(id, request, actor);
    }

    @GetMapping("/complaints/{id}/status/history")
    public List<StatusHistoryResponse> history(@PathVariable String id) {
        return statusService.history(id);
    }

    @GetMapping("/follow-up/months")
    public List<MonthlyFollowUpResponse> followUp(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return followUpService.find(from, to);
    }
}