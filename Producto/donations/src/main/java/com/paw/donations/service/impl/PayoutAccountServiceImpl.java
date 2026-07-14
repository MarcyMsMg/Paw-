package com.paw.donations.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paw.donations.dto.request.UpsertPayoutAccountRequest;
import com.paw.donations.dto.response.PayoutAccountResponse;
import com.paw.donations.exception.ResourceNotFoundException;
import com.paw.donations.model.PayoutAccount;
import com.paw.donations.repository.PayoutAccountRepository;
import com.paw.donations.service.PayoutAccountService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayoutAccountServiceImpl implements PayoutAccountService {

    private final PayoutAccountRepository payoutAccountRepository;

    @Override
    public PayoutAccountResponse getByNgo(UUID ngoId) {
        PayoutAccount account = payoutAccountRepository.findByNgoId(ngoId)
                .orElseThrow(() -> new ResourceNotFoundException("La ONG aún no tiene datos de transferencia"));
        return toResponse(account);
    }

    @Override
    @Transactional
    public PayoutAccountResponse upsert(UUID ngoId, UpsertPayoutAccountRequest request) {
        PayoutAccount account = payoutAccountRepository.findByNgoId(ngoId)
                .orElseGet(() -> PayoutAccount.builder().ngoId(ngoId).build());

        account.setHolderName(request.holderName());
        account.setRut(request.rut());
        account.setBankName(request.bankName());
        account.setAccountType(request.accountType());
        account.setAccountNumber(request.accountNumber());
        account.setEmail(request.email());

        return toResponse(payoutAccountRepository.save(account));
    }

    private PayoutAccountResponse toResponse(PayoutAccount a) {
        return new PayoutAccountResponse(
                a.getId(),
                a.getNgoId(),
                a.getHolderName(),
                a.getRut(),
                a.getBankName(),
                a.getAccountType(),
                a.getAccountNumber(),
                a.getEmail(),
                a.getUpdatedAt()
        );
    }
}
