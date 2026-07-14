package com.paw.feed.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.feed.client.NgoProfileResponse;
import com.paw.feed.client.NotificationEventRequest;
import com.paw.feed.client.NotificationsClient;
import com.paw.feed.client.UserAccessResponse;
import com.paw.feed.client.UsersClient;
import com.paw.feed.domain.FeedPost;
import com.paw.feed.domain.FeedPostStatus;
import com.paw.feed.domain.FeedPostType;
import com.paw.feed.dto.CreateFeedPostRequest;
import com.paw.feed.dto.FeedPostResponse;
import com.paw.feed.dto.UpdateFeedPostRequest;
import com.paw.feed.exception.ApiException;
import com.paw.feed.repository.FeedPostRepository;
import com.paw.feed.security.AuthenticatedUser;
import com.paw.feed.security.CurrentUserProvider;
import com.paw.feed.security.UserRole;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedPostService {

    private final FeedPostRepository feedPostRepository;
    private final FeedPostMapper feedPostMapper;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccessValidator userAccessValidator;
    private final UsersClient usersClient;
    private final NotificationsClient notificationsClient;

    @Transactional(readOnly = true)
    public List<FeedPostResponse> listPublic(UUID ngoId, FeedPostType type, String search) {
        Sort sort = Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("createdAt"));
        List<FeedPost> posts = feedPostRepository.findAll(publicSpecification(ngoId, type, search), sort);
        return mapAll(posts);
    }

    @Transactional(readOnly = true)
    public FeedPostResponse getPublic(UUID postId) {
        FeedPost post = feedPostRepository.findById(postId)
                .filter(item -> item.getStatus() == FeedPostStatus.PUBLISHED)
                .orElseThrow(() -> ApiException.notFound("Feed post not found"));
        return feedPostMapper.toResponse(post, getNgoProfile(post.getNgoId(), new HashMap<>()));
    }

    @Transactional(readOnly = true)
    public List<FeedPostResponse> listOwned() {
        AuthenticatedUser user = requireActiveNgo();
        return mapAll(feedPostRepository.findByNgoIdOrderByUpdatedAtDesc(user.id()));
    }

    @Transactional
    public FeedPostResponse create(CreateFeedPostRequest request) {
        AuthenticatedUser authenticatedUser = currentUserProvider.get();
        requireRole(authenticatedUser, UserRole.NGO);
        UserAccessResponse ngo = userAccessValidator.requireActive(authenticatedUser);

        FeedPost post = new FeedPost();
        post.setNgoId(authenticatedUser.id());
        applyCreateRequest(post, request);
        if (Boolean.TRUE.equals(request.publishNow())) {
            post.setStatus(FeedPostStatus.PUBLISHED);
            post.setPublishedAt(Instant.now());
        } else {
            post.setStatus(FeedPostStatus.DRAFT);
            post.setPublishedAt(null);
        }

        FeedPost saved = feedPostRepository.save(post);
        if (saved.getStatus() == FeedPostStatus.PUBLISHED) {
            notifyPostPublished(saved, "feed.post.published." + saved.getId());
        }
        return feedPostMapper.toResponse(saved, new NgoProfileResponse(ngo.id(), ngo.ngoName(), null));
    }

    @Transactional
    public FeedPostResponse update(UUID postId, UpdateFeedPostRequest request) {
        AuthenticatedUser user = requireActiveNgo();
        FeedPost post = getOwnedPost(postId, user.id());
        applyUpdateRequest(post, request);
        return feedPostMapper.toResponse(post, getNgoProfile(post.getNgoId(), new HashMap<>()));
    }

    @Transactional
    public FeedPostResponse publish(UUID postId) {
        AuthenticatedUser user = requireActiveNgo();
        FeedPost post = getOwnedPost(postId, user.id());

        if (post.getStatus() == FeedPostStatus.ARCHIVED || post.getStatus() == FeedPostStatus.HIDDEN) {
            throw ApiException.badRequest("Archived or hidden posts cannot be published");
        }
        if (post.getStatus() != FeedPostStatus.PUBLISHED) {
            post.setStatus(FeedPostStatus.PUBLISHED);
            post.setPublishedAt(Instant.now());
            notifyPostPublished(post, "feed.post.published." + post.getId());
        }
        return feedPostMapper.toResponse(post, getNgoProfile(post.getNgoId(), new HashMap<>()));
    }

    @Transactional
    public FeedPostResponse archive(UUID postId) {
        AuthenticatedUser user = requireActiveNgo();
        FeedPost post = getOwnedPost(postId, user.id());
        if (post.getStatus() != FeedPostStatus.ARCHIVED) {
            post.setStatus(FeedPostStatus.ARCHIVED);
            notifyPostArchived(post);
        }
        return feedPostMapper.toResponse(post, getNgoProfile(post.getNgoId(), new HashMap<>()));
    }

    @Transactional(readOnly = true)
    public List<FeedPostResponse> listAllForAdmin() {
        AuthenticatedUser user = currentUserProvider.get();
        requireRole(user, UserRole.ADMIN);
        userAccessValidator.requireActive(user);
        return mapAll(feedPostRepository.findAll(Sort.by(Sort.Order.desc("createdAt"))));
    }

    @Transactional
    public FeedPostResponse hide(UUID postId) {
        AuthenticatedUser user = currentUserProvider.get();
        requireRole(user, UserRole.ADMIN);
        userAccessValidator.requireActive(user);
        FeedPost post = feedPostRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("Feed post not found"));
        post.setStatus(FeedPostStatus.HIDDEN);
        return feedPostMapper.toResponse(post, getNgoProfile(post.getNgoId(), new HashMap<>()));
    }

    private void notifyPostPublished(FeedPost post, String eventId) {
        notificationsClient.send(new NotificationEventRequest(
                eventId,
                "FEED_POST_PUBLISHED",
                Instant.now(),
                post.getNgoId(),
                "NGO",
                "Publicacion publicada",
                "Tu actualizacion ya esta visible en el feed publico.",
                "FEED_POST",
                post.getId(),
                "/feed/" + post.getId(),
                postMetadata(post)
        ));
    }

    private void notifyPostArchived(FeedPost post) {
        notificationsClient.send(new NotificationEventRequest(
                "feed.post.archived." + post.getId() + "." + Instant.now().toEpochMilli(),
                "FEED_POST_ARCHIVED",
                Instant.now(),
                post.getNgoId(),
                "NGO",
                "Publicacion archivada",
                "Tu actualizacion fue archivada y ya no aparece publicamente.",
                "FEED_POST",
                post.getId(),
                "/ong/publicaciones",
                postMetadata(post)
        ));
    }

    private String postMetadata(FeedPost post) {
        String title = post.getTitle() == null ? "" : post.getTitle().replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"title\":\"" + title + "\",\"type\":\"" + post.getType() + "\",\"status\":\"" + post.getStatus() + "\"}";
    }

    private List<FeedPostResponse> mapAll(List<FeedPost> posts) {
        Map<UUID, NgoProfileResponse> profiles = new HashMap<>();
        return posts.stream()
                .map(post -> feedPostMapper.toResponse(post, getNgoProfile(post.getNgoId(), profiles)))
                .toList();
    }

    private NgoProfileResponse getNgoProfile(UUID ngoId, Map<UUID, NgoProfileResponse> profiles) {
        if (profiles.containsKey(ngoId)) {
            return profiles.get(ngoId);
        }
        NgoProfileResponse profile = usersClient.findNgoProfile(ngoId).orElse(null);
        profiles.put(ngoId, profile);
        return profile;
    }

    private AuthenticatedUser requireActiveNgo() {
        AuthenticatedUser user = currentUserProvider.get();
        requireRole(user, UserRole.NGO);
        userAccessValidator.requireActive(user);
        return user;
    }

    private FeedPost getOwnedPost(UUID postId, UUID ngoId) {
        FeedPost post = feedPostRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("Feed post not found"));
        if (!post.getNgoId().equals(ngoId)) {
            throw ApiException.forbidden("You cannot manage posts from another NGO");
        }
        return post;
    }

    private void applyCreateRequest(FeedPost post, CreateFeedPostRequest request) {
        post.setTitle(request.title().trim());
        post.setSummary(trimToNull(request.summary()));
        post.setContent(request.content().trim());
        post.setType(request.type());
        replaceImages(post, request.imageUrls());
        post.setVideoUrl(trimToNull(request.videoUrl()));
        post.setRelatedAnimalId(request.relatedAnimalId());
        post.setRelatedCampaignId(request.relatedCampaignId());
    }

    private void applyUpdateRequest(FeedPost post, UpdateFeedPostRequest request) {
        post.setTitle(request.title().trim());
        post.setSummary(trimToNull(request.summary()));
        post.setContent(request.content().trim());
        post.setType(request.type());
        replaceImages(post, request.imageUrls());
        post.setVideoUrl(trimToNull(request.videoUrl()));
        post.setRelatedAnimalId(request.relatedAnimalId());
        post.setRelatedCampaignId(request.relatedCampaignId());
    }

    private void replaceImages(FeedPost post, List<String> imageUrls) {
        post.getImageUrls().clear();
        post.getImageUrls().addAll(cleanUrls(imageUrls));
    }

    private List<String> cleanUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return new ArrayList<>();
        }
        return imageUrls.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private Specification<FeedPost> publicSpecification(UUID ngoId, FeedPostType type, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), FeedPostStatus.PUBLISHED));
            if (ngoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("ngoId"), ngoId));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            String normalizedSearch = trimToNull(search);
            if (normalizedSearch != null) {
                String pattern = "%" + normalizedSearch.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), pattern)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void requireRole(AuthenticatedUser user, UserRole requiredRole) {
        if (user.role() != requiredRole) {
            throw ApiException.forbidden("This operation requires role " + requiredRole);
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
