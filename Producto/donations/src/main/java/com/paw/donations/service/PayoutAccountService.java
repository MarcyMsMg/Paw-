package com.paw.donations.service;

import java.util.UUID;

import com.paw.donations.dto.request.UpsertPayoutAccountRequest;
import com.paw.donations.dto.response.PayoutAccountResponse;

public interface PayoutAccountService {

    // Devuelve los datos de transferencia de una ONG (404 si aún no los cargó).
    PayoutAccountResponse getByNgo(UUID ngoId);

    // Crea o actualiza los datos de transferencia de una ONG.
    PayoutAccountResponse upsert(UUID ngoId, UpsertPayoutAccountRequest request);
}
