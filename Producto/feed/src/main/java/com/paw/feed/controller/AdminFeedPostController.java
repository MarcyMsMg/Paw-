package com.paw.feed.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.feed.common.ApiResponse;
import com.paw.feed.dto.FeedPostResponse;
import com.paw.feed.service.FeedPostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedPostController {

    private final FeedPostService feedPostService;

    @GetMapping
    public ApiResponse<List<FeedPostResponse>> listAll() {
        return ApiResponse.ok(feedPostService.listAllForAdmin());
    }

    @PatchMapping("/{postId}/hide")
    public ApiResponse<FeedPostResponse> hide(@PathVariable UUID postId) {
        return ApiResponse.ok("Feed post hidden", feedPostService.hide(postId));
    }
}
