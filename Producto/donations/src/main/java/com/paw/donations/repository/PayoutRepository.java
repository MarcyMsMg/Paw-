package com.paw.donations.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.donations.model.Payout;

public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    List<Payout> findByNgoId(UUID ngoId);
}
