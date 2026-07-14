package com.paw.feed.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paw.feed.common.ApiResponse;
import com.paw.feed.domain.FeedPostType;
import com.paw.feed.dto.FeedPostResponse;
import com.paw.feed.service.FeedPostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed/posts")
@RequiredArgsConstructor
public class FeedPostController {

    private final FeedPostService feedPostService;

    @GetMapping
    public ApiResponse<List<FeedPostResponse>> list(
            @RequestParam(required = false) UUID ngoId,
            @RequestParam(required = false) FeedPostType type,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(feedPostService.listPublic(ngoId, type, search));
    }

    @GetMapping("/{postId}")
    public ApiResponse<FeedPostResponse> findById(@PathVariable UUID postId) {
        return ApiResponse.ok(feedPostService.getPublic(postId));
    }
}
