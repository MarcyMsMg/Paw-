package com.paw.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email @NotBlank @Size(max = 120) String email,
        @NotBlank @Size(max = 72) String password
    )
    {
}