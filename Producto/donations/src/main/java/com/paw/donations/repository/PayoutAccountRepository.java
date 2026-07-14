package com.paw.donations.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.donations.model.PayoutAccount;

public interface PayoutAccountRepository extends JpaRepository<PayoutAccount, UUID> {

    Optional<PayoutAccount> findByNgoId(UUID ngoId);
}
