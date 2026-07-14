package com.paw.feed.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.feed.common.ApiResponse;
import com.paw.feed.dto.CreateFeedPostRequest;
import com.paw.feed.dto.FeedPostResponse;
import com.paw.feed.dto.UpdateFeedPostRequest;
import com.paw.feed.service.FeedPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed/ngo/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NGO')")
public class NgoFeedPostController {

    private final FeedPostService feedPostService;

    @GetMapping
    public ApiResponse<List<FeedPostResponse>> listOwned() {
        return ApiResponse.ok(feedPostService.listOwned());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedPostResponse>> create(
            @Valid @RequestBody CreateFeedPostRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feed post created", feedPostService.create(request)));
    }

    @PutMapping("/{postId}")
    public ApiResponse<FeedPostResponse> update(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdateFeedPostRequest request
    ) {
        return ApiResponse.ok("Feed post updated", feedPostService.update(postId, request));
    }

    @PatchMapping("/{postId}/publish")
    public ApiResponse<FeedPostResponse> publish(@PathVariable UUID postId) {
        return ApiResponse.ok("Feed post published", feedPostService.publish(postId));
    }

    @PatchMapping("/{postId}/archive")
    public ApiResponse<FeedPostResponse> archive(@PathVariable UUID postId) {
        return ApiResponse.ok("Feed post archived", feedPostService.archive(postId));
    }
}
