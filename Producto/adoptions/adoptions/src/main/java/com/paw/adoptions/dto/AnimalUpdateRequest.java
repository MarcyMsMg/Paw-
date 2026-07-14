package com.paw.adoptions.dto;

import java.util.List;
import java.util.UUID;

import com.paw.adoptions.domain.AnimalStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AnimalUpdateRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Size(min = 2, max = 80) String species,
        @NotBlank @Size(max = 80) String age,
        @NotBlank @Size(max = 40) String sex,
        @NotBlank @Size(max = 40) String size,
        @Size(max = 160) String location,
        @Size(max = 500) String healthStatus,
        @NotBlank @Size(min = 20, max = 2000) String description,
        @Size(max = 1000) String adoptionRequirements,
        @NotEmpty @Size(max = 5) List<@NotBlank @Size(max = 1000) String> photoUrls,
        AnimalStatus status,
        Boolean published,
        UUID formTemplateId
) {
}
