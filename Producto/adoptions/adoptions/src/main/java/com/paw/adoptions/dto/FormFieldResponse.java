package com.paw.adoptions.dto;

import com.paw.adoptions.domain.FormFieldType;

import java.util.List;

public record FormFieldResponse(
        String key,
        String label,
        FormFieldType type,
        boolean required,
        boolean system,
        String placeholder,
        List<String> options,
        int order
) {
}
