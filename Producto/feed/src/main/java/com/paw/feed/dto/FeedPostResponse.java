package com.paw.feed.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.paw.feed.domain.FeedPostStatus;
import com.paw.feed.domain.FeedPostType;

public record FeedPostResponse(
        UUID id,
        UUID ngoId,
        String ngoName,
        String ngoLogoUrl,
        String title,
        String summary,
        String content,
        FeedPostType type,
        List<String> imageUrls,
        String videoUrl,
        UUID relatedAnimalId,
        UUID relatedCampaignId,
        FeedPostStatus status,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
