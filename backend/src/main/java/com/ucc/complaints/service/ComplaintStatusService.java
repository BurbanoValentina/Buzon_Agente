package com.ucc.complaints.service;

import com.ucc.complaints.dto.status.StatusHistoryResponse;
import com.ucc.complaints.dto.status.StatusTransitionRequest;
import com.ucc.complaints.model.*;
import com.ucc.complaints.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ComplaintStatusService {
    private final ComplaintRepository complaints;
    private final StatusHistoryRepository histories;
    private final MonthlyEventRepository monthlyEvents;
    private final AuditService auditService;

    public ComplaintStatusService(ComplaintRepository complaints,
                                  StatusHistoryRepository histories,
                                  MonthlyEventRepository monthlyEvents,
                                  AuditService auditService) {
        this.complaints = complaints;
        this.histories = histories;
        this.monthlyEvents = monthlyEvents;
        this.auditService = auditService;
    }

    @Transactional
    public List<StatusHistoryResponse> transition(String publicId, StatusTransitionRequest request,
                                                   String actor) {
        Complaint complaint = complaints.findByPublicId(publicId)
            .orElseThrow(() -> new ComplaintValidationException("La queja no existe."));
        ComplaintStatus previous = complaint.getStatus();
        ComplaintStatus next = request.status();
        validateTransition(previous, next);
        String comment = request.comment() == null ? "" : request.comment().trim();
        if (comment.isBlank()) {
            throw new ComplaintValidationException("El comentario es obligatorio.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        boolean previouslyReopened = histories.findByComplaintIdOrderByChangedAtAsc(complaint.getId())
            .stream().anyMatch(history -> ComplaintStatus.COMPLETADA.name()
                .equals(history.getPreviousStatus())
                && ComplaintStatus.PENDIENTE.name().equals(history.getNewStatus()));

        complaint.setStatus(next);
        if (next == ComplaintStatus.COMPLETADA && complaint.getCompletedAt() == null) {
            complaint.setCompletedAt(now);
        }
        complaints.save(complaint);
        histories.save(new StatusHistory(complaint, previous, next, comment,
            actor == null || actor.isBlank() ? "administrator" : actor, now));

        if (next == ComplaintStatus.COMPLETADA) {
            String eventType = previouslyReopened ? "RE_COMPLETION" : "FIRST_COMPLETION";
            monthlyEvents.save(new MonthlyEvent(complaint, eventType,
                now.toLocalDate().withDayOfMonth(1), now));
        } else if (previous == ComplaintStatus.COMPLETADA && next == ComplaintStatus.PENDIENTE) {
            monthlyEvents.save(new MonthlyEvent(complaint, "REOPENING",
                now.toLocalDate().withDayOfMonth(1), now));
        }
        auditService.record("STATUS_CHANGE", actor);
        return histories.findByComplaintIdOrderByChangedAtAsc(complaint.getId())
            .stream().map(com.ucc.complaints.dto.status.StatusHistoryResponse::from).toList();
    }

    public List<StatusHistoryResponse> history(String publicId) {
        Complaint complaint = complaints.findByPublicId(publicId)
            .orElseThrow(() -> new ComplaintValidationException("La queja no existe."));
        return histories.findByComplaintIdOrderByChangedAtAsc(complaint.getId())
            .stream().map(StatusHistoryResponse::from).toList();
    }

    private void validateTransition(ComplaintStatus previous, ComplaintStatus next) {
        boolean valid = switch (previous) {
            case PENDIENTE -> next == ComplaintStatus.COMPLETADA
                || next == ComplaintStatus.NO_COMPLETADA;
            case NO_COMPLETADA -> next == ComplaintStatus.PENDIENTE
                || next == ComplaintStatus.COMPLETADA;
            case COMPLETADA -> next == ComplaintStatus.PENDIENTE;
        };
        if (!valid) {
            throw new ComplaintValidationException("La transición de estado no está permitida.");
        }
    }
}