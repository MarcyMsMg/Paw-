package com.paw.users.service;

import java.util.List;
import java.util.UUID;

import com.paw.users.dto.request.UpdateUserProfileRequest;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.dto.response.InternalUserAccessResponse;

public interface UserService {

    UserResponse findById(UUID userId);

    UserResponse findActiveNgoById(UUID userId);

    InternalUserAccessResponse findAccessById(UUID userId);

    UserResponse updateProfile(UUID userId, UpdateUserProfileRequest request);

    List<UserResponse> findAllActiveNgos();
}
