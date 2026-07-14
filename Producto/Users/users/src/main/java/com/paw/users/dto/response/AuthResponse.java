package com.paw.users.dto.response;

public record AuthResponse(
        String token,
        UserResponse user
) {
}