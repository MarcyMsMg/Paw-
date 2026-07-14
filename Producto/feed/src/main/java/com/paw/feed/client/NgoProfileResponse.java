package com.paw.feed.client;

import java.util.UUID;

public record NgoProfileResponse(
        UUID id,
        String ngoName,
        String profileImageUrl
) {
}
