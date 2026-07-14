package com.paw.users.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.paw.users.dto.request.UpdateUserStatusRequest;
import com.paw.users.dto.response.ApiResponse;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.enums.UserRole;
import com.paw.users.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll(
            @RequestParam(required = false) UserRole role
    ) {
        return new ApiResponse<>(
                true,
                "Usuarios encontrados",
                adminUserService.findAll(role)
        );
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<UserResponse> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest solicitud
    ) {
        return new ApiResponse<>(
                true,
                "Estado del usuario actualizado correctamente",
                adminUserService.updateStatus(userId, solicitud)
        );
    }
}
