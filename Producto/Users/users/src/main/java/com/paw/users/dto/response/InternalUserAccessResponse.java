package com.paw.users.dto.response;

import java.util.UUID;

import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;

public record InternalUserAccessResponse(
        UUID id,
        String email,
        UserRole role,
        AccountStatus status,
        String ngoName,
        String location
) {
}
