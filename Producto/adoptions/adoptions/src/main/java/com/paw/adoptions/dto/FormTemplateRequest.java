package com.paw.adoptions.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FormTemplateRequest(
        @NotBlank @Size(min = 3, max = 120) String name,
        @Size(max = 500) String description,
        @NotNull Boolean active,
        // Sin @Size(min=1): una plantilla puede usar solo los campos base, sin campos personalizados.
        @NotNull @Size(max = 30) List<@Valid FormFieldRequest> fields
) {
}
