package com.paw.users.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.paw.users.dto.request.UpdateUserProfileRequest;
import com.paw.users.dto.response.ApiResponse;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id() or hasRole('ADMIN')")
    public ApiResponse<UserResponse> findById(@PathVariable UUID userId) {
        return new ApiResponse<>(
                true,
                "Usuario encontrado",
                userService.findById(userId)
        );
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id() or hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileRequest solicitud
    ) {
        return new ApiResponse<>(
                true,
                "Perfil del usuario actualizado correctamente",
                userService.updateProfile(userId, solicitud)
        );
    }
}
