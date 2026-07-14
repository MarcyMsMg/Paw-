package com.paw.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NaturalPersonRegisterRequest(
        @NotBlank @Size(min = 2, max = 80) String firstName,
        @NotBlank @Size(min = 2, max = 80) String lastName,
        @Email @NotBlank @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must contain at least one lowercase letter, one uppercase letter and one number"
        )
        String password,
        @Size(max = 1000) String profileImageUrl
    )

    {
}

//usamos record para tener un código más limpio, solo queremos transportar datos
//no necesitamos lógica adicional