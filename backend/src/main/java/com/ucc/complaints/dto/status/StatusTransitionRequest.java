package com.ucc.complaints.dto.status;

import com.ucc.complaints.model.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StatusTransitionRequest(
    @NotNull ComplaintStatus status,
    @NotBlank(message = "El comentario es obligatorio.") String comment
) { }