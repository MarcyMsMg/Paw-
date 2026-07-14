package com.paw.users.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Actualizacion parcial (PATCH): todos los campos son opcionales (null = no se modifica),
// pero si vienen con un valor no nulo, se validan.
public record UpdateUserProfileRequest(
        @Size(min = 2, max = 80) String firstName,
        @Size(min = 2, max = 80) String lastName,
        @Size(min = 3, max = 120) String ngoName,
        @Size(max = 1000) String profileImageUrl,
        @Size(min = 20, max = 2000) String description,
        @Size(max = 1000) String coverImageUrl,
        @Size(min = 3, max = 160) String location,
        @Min(1900) Integer foundationYear,
        @PositiveOrZero Integer rescuedAnimalsCount,
        @PositiveOrZero Integer volunteersCount
) {
}
