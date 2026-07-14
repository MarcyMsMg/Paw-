package com.paw.adoptions.dto;

import com.paw.adoptions.domain.AdoptionApplicationStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdoptionApplicationDecisionRequest(
        @NotNull AdoptionApplicationStatus status,
        @Size(max = 1000) String ngoResponse
) {
}
