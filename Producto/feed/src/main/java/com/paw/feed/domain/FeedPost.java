package com.paw.feed.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "feed_posts",
        indexes = {
                @Index(name = "idx_feed_posts_ngo", columnList = "ngo_id"),
                @Index(name = "idx_feed_posts_status", columnList = "status"),
                @Index(name = "idx_feed_posts_type", columnList = "type"),
                @Index(name = "idx_feed_posts_published_at", columnList = "published_at")
        }
)
public class FeedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 600)
    private String summary;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FeedPostType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "feed_post_images",
            joinColumns = @JoinColumn(name = "feed_post_id")
    )
    @Column(name = "url", nullable = false, length = 1000)
    @OrderColumn(name = "position")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "related_animal_id")
    private UUID relatedAnimalId;

    @Column(name = "related_campaign_id")
    private UUID relatedCampaignId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FeedPostStatus status = FeedPostStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
