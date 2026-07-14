package com.paw.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record NgoRegisterRequest(
        @NotBlank @Size(min = 3, max = 120) String ngoName,
        @Email @NotBlank @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must contain at least one lowercase letter, one uppercase letter and one number"
        )
        String password,
        @Size(max = 1000) String profileImageUrl,
        @Size(max = 1000) String coverImageUrl,
        @NotBlank @Size(max = 8000000) String constitutionActUrl,
        @NotBlank @Size(min = 20, max = 2000) String description,
        @NotBlank @Size(min = 3, max = 160) String location,
        @Min(1900) Integer foundationYear,
        @PositiveOrZero Integer rescuedAnimalsCount,
        @PositiveOrZero Integer volunteersCount
        )
    {
}