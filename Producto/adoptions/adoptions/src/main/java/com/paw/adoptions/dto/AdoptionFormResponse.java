package com.paw.adoptions.dto;

import java.util.List;
import java.util.UUID;

public record AdoptionFormResponse(
        UUID templateId,
        String name,
        int revision,
        int maxCustomFields,
        List<FormFieldResponse> fields
) {
}
