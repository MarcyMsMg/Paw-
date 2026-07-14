package com.paw.users.service;

import java.util.List;
import java.util.UUID;

import com.paw.users.dto.request.UpdateUserStatusRequest;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.enums.UserRole;

public interface AdminUserService {

    List<UserResponse> findAll(UserRole role);

    UserResponse updateStatus(UUID userId, UpdateUserStatusRequest request);
}
