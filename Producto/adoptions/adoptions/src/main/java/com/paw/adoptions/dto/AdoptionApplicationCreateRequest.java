package com.paw.adoptions.dto;

import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdoptionApplicationCreateRequest(
        @NotBlank @Size(min = 3, max = 160) String fullName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 40) String phone,
        @NotBlank @Size(min = 5, max = 220) String address,
        @NotBlank @Size(max = 120) String housingType,
        @NotBlank @Size(max = 120) String otherAnimals,
        @NotBlank @Size(min = 20, max = 2000) String motivation,
        @Size(max = 500) String availability,
        @Size(max = 1000) String previousExperience,
        @Size(max = 30) Map<@Size(max = 50) String, @Size(max = 4000) String> customAnswers
) {
    public AdoptionApplicationCreateRequest(
            String fullName,
            String email,
            String phone,
            String address,
            String housingType,
            String otherAnimals,
            String motivation,
            String availability,
            String previousExperience
    ) {
        this(fullName, email, phone, address, housingType, otherAnimals, motivation,
                availability, previousExperience, Map.of());
    }
}
