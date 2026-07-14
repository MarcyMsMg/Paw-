package com.paw.notifications.client;

import com.paw.notifications.domain.AccountStatus;
import com.paw.notifications.domain.RecipientRole;

public record UserAccessResponse(
        java.util.UUID id,
        String email,
        RecipientRole role,
        AccountStatus status,
        String ngoName,
        String location
) {
}