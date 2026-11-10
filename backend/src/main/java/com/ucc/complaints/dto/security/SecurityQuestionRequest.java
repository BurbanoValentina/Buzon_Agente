package com.ucc.complaints.dto.security;

import jakarta.validation.constraints.NotBlank;

public record SecurityQuestionRequest(@NotBlank String username, @NotBlank String answer) { }
