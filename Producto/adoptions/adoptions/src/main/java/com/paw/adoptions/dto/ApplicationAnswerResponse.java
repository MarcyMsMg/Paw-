package com.paw.adoptions.dto;

import com.paw.adoptions.domain.FormFieldType;

public record ApplicationAnswerResponse(
        String key,
        String label,
        FormFieldType type,
        String value,
        int order
) {
}
