package com.paw.users.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String ngoName,
        String profileImageUrl,
        String description,
        String coverImageUrl,
        String location,
        Integer foundationYear,
        Integer rescuedAnimalsCount,
        Integer volunteersCount,
        UserRole role,
        AccountStatus status,
        LocalDateTime createdAt
) {
}
