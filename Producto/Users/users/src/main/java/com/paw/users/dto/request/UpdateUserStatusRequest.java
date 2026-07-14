package com.paw.users.dto.request;

import com.paw.users.enums.AccountStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull AccountStatus status
) {
}
