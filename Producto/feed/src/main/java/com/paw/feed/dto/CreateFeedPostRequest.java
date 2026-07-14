package com.paw.feed.dto;

import java.util.List;
import java.util.UUID;

import com.paw.feed.domain.FeedPostType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFeedPostRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 600) String summary,
        @NotBlank @Size(max = 10000) String content,
        @NotNull FeedPostType type,
        List<@NotBlank @Size(max = 1000) String> imageUrls,
        @Size(max = 1000) String videoUrl,
        UUID relatedAnimalId,
        UUID relatedCampaignId,
        Boolean publishNow
) {
}
