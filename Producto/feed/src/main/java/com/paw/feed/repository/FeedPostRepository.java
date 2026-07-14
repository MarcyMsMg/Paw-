package com.paw.feed.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.paw.feed.domain.FeedPost;

public interface FeedPostRepository extends JpaRepository<FeedPost, UUID>, JpaSpecificationExecutor<FeedPost> {

    List<FeedPost> findByNgoIdOrderByUpdatedAtDesc(UUID ngoId);
}
