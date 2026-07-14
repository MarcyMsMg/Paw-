package com.paw.users.service;

import java.util.List;
import java.util.UUID;

import com.paw.users.dto.request.RejectNgoRequest;
import com.paw.users.dto.response.NgoRegistrationRequestResponse;

public interface AdminNgoRequestService {

    List<NgoRegistrationRequestResponse> findAll();

    List<NgoRegistrationRequestResponse> findPending();

    NgoRegistrationRequestResponse approve(UUID requestId);

    NgoRegistrationRequestResponse reject(UUID requestId, RejectNgoRequest request);
}