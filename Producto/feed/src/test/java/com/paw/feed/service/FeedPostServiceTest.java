package com.paw.feed.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.paw.feed.client.NgoProfileResponse;
import com.paw.feed.client.NotificationEventRequest;
import com.paw.feed.client.NotificationsClient;
import com.paw.feed.client.UserAccessResponse;
import com.paw.feed.client.UsersClient;
import com.paw.feed.domain.FeedPost;
import com.paw.feed.domain.FeedPostStatus;
import com.paw.feed.domain.FeedPostType;
import com.paw.feed.dto.CreateFeedPostRequest;
import com.paw.feed.dto.UpdateFeedPostRequest;
import com.paw.feed.exception.ApiException;
import com.paw.feed.repository.FeedPostRepository;
import com.paw.feed.security.AccountStatus;
import com.paw.feed.security.AuthenticatedUser;
import com.paw.feed.security.CurrentUserProvider;
import com.paw.feed.security.UserRole;

@ExtendWith(MockitoExtension.class)
class FeedPostServiceTest {

    @Mock private FeedPostRepository feedPostRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserAccessValidator userAccessValidator;
    @Mock private UsersClient usersClient;
    @Mock private NotificationsClient notificationsClient;

    private FeedPostService feedPostService;
    private FeedPostMapper mapper;
    private UUID ngoId;
    private AuthenticatedUser ngoUser;

    @BeforeEach
    void setUp() {
        mapper = new FeedPostMapper();
        feedPostService = new FeedPostService(
                feedPostRepository,
                mapper,
                currentUserProvider,
                userAccessValidator,
                usersClient,
                notificationsClient
        );
        ngoId = UUID.randomUUID();
        ngoUser = new AuthenticatedUser(ngoId, "ngo@test.local", UserRole.NGO);
    }

    @Test
    void create_shouldPublishImmediatelyCleanImagesAndNotify() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(feedPostRepository.save(any(FeedPost.class))).thenAnswer(invocation -> {
            FeedPost post = invocation.getArgument(0);
            post.setId(UUID.randomUUID());
            return post;
        });
        CreateFeedPostRequest request = new CreateFeedPostRequest(
                "  Rescate exitoso  ", "  Resumen  ", "  Contenido del rescate  ", FeedPostType.RESCUE,
                List.of(" https://img.test/a.jpg ", "", "https://img.test/a.jpg", "https://img.test/b.jpg"),
                "  https://youtu.be/video  ", UUID.randomUUID(), UUID.randomUUID(), true
        );

        // Act
        var response = feedPostService.create(request);

        // Assert
        ArgumentCaptor<FeedPost> postCaptor = ArgumentCaptor.forClass(FeedPost.class);
        ArgumentCaptor<NotificationEventRequest> eventCaptor = ArgumentCaptor.forClass(NotificationEventRequest.class);
        verify(feedPostRepository).save(postCaptor.capture());
        verify(notificationsClient).send(eventCaptor.capture());
        FeedPost saved = postCaptor.getValue();
        assertEquals(FeedPostStatus.PUBLISHED, saved.getStatus());
        assertNotNull(saved.getPublishedAt());
        assertEquals(List.of("https://img.test/a.jpg", "https://img.test/b.jpg"), saved.getImageUrls());
        assertEquals("Rescate exitoso", response.title());
        assertEquals("Patitas", response.ngoName());
        assertEquals("FEED_POST_PUBLISHED", eventCaptor.getValue().type());
    }

    @Test
    void create_shouldStoreDraftWithoutNotificationWhenPublishNowIsFalse() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(feedPostRepository.save(any(FeedPost.class))).thenAnswer(invocation -> {
            FeedPost post = invocation.getArgument(0);
            post.setId(UUID.randomUUID());
            return post;
        });

        // Act
        var response = feedPostService.create(createRequest(false));

        // Assert
        assertEquals(FeedPostStatus.DRAFT, response.status());
        verify(notificationsClient, never()).send(any());
    }

    @Test
    void publish_shouldRejectArchivedPosts() {
        // Arrange
        UUID postId = UUID.randomUUID();
        FeedPost post = post(postId, ngoId, FeedPostStatus.ARCHIVED);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(feedPostRepository.findById(postId)).thenReturn(Optional.of(post));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> feedPostService.publish(postId));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void archive_shouldChangeStatusAndNotify() {
        // Arrange
        UUID postId = UUID.randomUUID();
        FeedPost post = post(postId, ngoId, FeedPostStatus.PUBLISHED);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(feedPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(usersClient.findNgoProfile(ngoId)).thenReturn(Optional.of(new NgoProfileResponse(ngoId, "Patitas", "logo.png")));

        // Act
        var response = feedPostService.archive(postId);

        // Assert
        assertEquals(FeedPostStatus.ARCHIVED, post.getStatus());
        assertEquals(FeedPostStatus.ARCHIVED, response.status());
        verify(notificationsClient).send(any(NotificationEventRequest.class));
    }

    @Test
    void update_shouldRejectPostOwnedByAnotherNgo() {
        // Arrange
        UUID postId = UUID.randomUUID();
        FeedPost post = post(postId, UUID.randomUUID(), FeedPostStatus.DRAFT);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(feedPostRepository.findById(postId)).thenReturn(Optional.of(post));

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> feedPostService.update(postId, updateRequest()));

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exception.status());
    }

    @Test
    void getPublic_shouldReturnPublishedPostWithNgoProfile() {
        // Arrange
        UUID postId = UUID.randomUUID();
        FeedPost post = post(postId, ngoId, FeedPostStatus.PUBLISHED);
        when(feedPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(usersClient.findNgoProfile(ngoId)).thenReturn(Optional.of(new NgoProfileResponse(ngoId, "Patitas", "logo.png")));

        // Act
        var response = feedPostService.getPublic(postId);

        // Assert
        assertEquals(postId, response.id());
        assertEquals("Patitas", response.ngoName());
    }

    @Test
    void feedPostMapper_shouldFallbackWhenNgoProfileIsMissing() {
        // Arrange
        FeedPost post = post(UUID.randomUUID(), ngoId, FeedPostStatus.PUBLISHED);

        // Act
        var response = mapper.toResponse(post, null);

        // Assert
        assertEquals(post.getId(), response.id());
        assertEquals(List.of("https://img.test/a.jpg"), response.imageUrls());
        assertEquals(null, response.ngoName());
    }

    @Test
    void listPublic_shouldMapPostsAndCacheNgoProfileLookup() {
        // Arrange
        FeedPost first = post(UUID.randomUUID(), ngoId, FeedPostStatus.PUBLISHED);
        FeedPost second = post(UUID.randomUUID(), ngoId, FeedPostStatus.PUBLISHED);
        when(feedPostRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(first, second));
        when(usersClient.findNgoProfile(ngoId)).thenReturn(Optional.of(new NgoProfileResponse(ngoId, "Patitas", "logo.png")));

        // Act
        var response = feedPostService.listPublic(ngoId, FeedPostType.GENERAL, "titulo");

        // Assert
        assertEquals(2, response.size());
        assertEquals("Patitas", response.getFirst().ngoName());
        verify(usersClient, times(1)).findNgoProfile(ngoId);
    }

    @Test
    void publish_shouldPublishDraftSetDateAndNotify() {
        // Arrange
        UUID postId = UUID.randomUUID();
        FeedPost post = post(postId, ngoId, FeedPostStatus.DRAFT);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(feedPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(usersClient.findNgoProfile(ngoId)).thenReturn(Optional.of(new NgoProfileResponse(ngoId, "Patitas", "logo.png")));

        // Act
        var response = feedPostService.publish(postId);

        // Assert
        assertEquals(FeedPostStatus.PUBLISHED, post.getStatus());
        assertNotNull(post.getPublishedAt());
        assertEquals(FeedPostStatus.PUBLISHED, response.status());
        verify(notificationsClient).send(any(NotificationEventRequest.class));
    }

    @Test
    void update_shouldApplyPayloadForOwnedPost() {
        // Arrange
        UUID postId = UUID.randomUUID();
        FeedPost post = post(postId, ngoId, FeedPostStatus.DRAFT);
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(userAccessValidator.requireActive(ngoUser)).thenReturn(new UserAccessResponse(
                ngoId, ngoUser.email(), ngoUser.role(), AccountStatus.ACTIVE, "Patitas", "Santiago"
        ));
        when(feedPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(usersClient.findNgoProfile(ngoId)).thenReturn(Optional.of(new NgoProfileResponse(ngoId, "Patitas", "logo.png")));

        // Act
        var response = feedPostService.update(postId, updateRequest());

        // Assert
        assertEquals("Titulo editado", post.getTitle());
        assertEquals(FeedPostType.SUCCESS_STORY, post.getType());
        assertEquals(List.of("https://img.test/c.jpg"), post.getImageUrls());
        assertEquals("Titulo editado", response.title());
    }

    @Test
    void listAllForAdmin_shouldRequireAdminAndReturnAllPosts() {
        // Arrange
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), "admin@test.local", UserRole.ADMIN);
        FeedPost post = post(UUID.randomUUID(), ngoId, FeedPostStatus.PUBLISHED);
        when(currentUserProvider.get()).thenReturn(admin);
        when(feedPostRepository.findAll(any(Sort.class))).thenReturn(List.of(post));
        when(usersClient.findNgoProfile(ngoId)).thenReturn(Optional.of(new NgoProfileResponse(ngoId, "Patitas", "logo.png")));

        // Act
        var response = feedPostService.listAllForAdmin();

        // Assert
        assertEquals(1, response.size());
        assertEquals("Patitas", response.getFirst().ngoName());
        verify(userAccessValidator).requireActive(admin);
    }

    @Test
    void hide_shouldRequireAdminAndSetStatusHidden() {
        // Arrange
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), "admin@test.local", UserRole.ADMIN);
        UUID postId = UUID.randomUUID();
        FeedPost post = post(postId, ngoId, FeedPostStatus.PUBLISHED);
        when(currentUserProvider.get()).thenReturn(admin);
        when(feedPostRepository.findById(postId)).thenReturn(Optional.of(post));
        when(usersClient.findNgoProfile(ngoId)).thenReturn(Optional.of(new NgoProfileResponse(ngoId, "Patitas", "logo.png")));

        // Act
        var response = feedPostService.hide(postId);

        // Assert
        assertEquals(FeedPostStatus.HIDDEN, post.getStatus());
        assertEquals(FeedPostStatus.HIDDEN, response.status());
        verify(userAccessValidator).requireActive(admin);
    }
    private CreateFeedPostRequest createRequest(boolean publishNow) {
        return new CreateFeedPostRequest(
                "Titulo", "Resumen", "Contenido", FeedPostType.GENERAL,
                List.of("https://img.test/a.jpg"), null, null, null, publishNow
        );
    }

    private UpdateFeedPostRequest updateRequest() {
        return new UpdateFeedPostRequest(
                "Titulo editado", "Resumen", "Contenido editado", FeedPostType.SUCCESS_STORY,
                List.of("https://img.test/c.jpg"), null, null, null
        );
    }

    private FeedPost post(UUID postId, UUID ownerNgoId, FeedPostStatus status) {
        FeedPost post = new FeedPost();
        post.setId(postId);
        post.setNgoId(ownerNgoId);
        post.setTitle("Titulo");
        post.setSummary("Resumen");
        post.setContent("Contenido");
        post.setType(FeedPostType.GENERAL);
        post.getImageUrls().add("https://img.test/a.jpg");
        post.setStatus(status);
        return post;
    }
}