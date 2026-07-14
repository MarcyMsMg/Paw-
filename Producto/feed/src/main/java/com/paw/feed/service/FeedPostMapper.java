package com.paw.feed.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.paw.feed.client.NgoProfileResponse;
import com.paw.feed.domain.FeedPost;
import com.paw.feed.dto.FeedPostResponse;

@Component
public class FeedPostMapper {

    public FeedPostResponse toResponse(FeedPost post, NgoProfileResponse ngoProfile) {
        return new FeedPostResponse(
                post.getId(),
                post.getNgoId(),
                ngoProfile == null ? null : ngoProfile.ngoName(),
                ngoProfile == null ? null : ngoProfile.profileImageUrl(),
                post.getTitle(),
                post.getSummary(),
                post.getContent(),
                post.getType(),
                post.getImageUrls() == null ? List.of() : List.copyOf(post.getImageUrls()),
                post.getVideoUrl(),
                post.getRelatedAnimalId(),
                post.getRelatedCampaignId(),
                post.getStatus(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
