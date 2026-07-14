package com.paw.adoptions.dto;

import com.paw.adoptions.domain.FormFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FormFieldRequest(
        @NotBlank @Size(min = 3, max = 160) String label,
        @NotNull FormFieldType type,
        boolean required,
        @Size(max = 200) String placeholder,
        @Size(max = 20) List<@NotBlank @Size(max = 160) String> options
) {
}
