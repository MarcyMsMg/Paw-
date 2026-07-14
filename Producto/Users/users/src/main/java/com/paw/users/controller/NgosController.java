package com.paw.users.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.users.dto.response.ApiResponse;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.service.UserService;

import lombok.RequiredArgsConstructor;

// Endpoints públicos del directorio de ONGs.
// La vista pública (Home, Fundaciones, FundacionDetail) los consume sin necesidad
// de estar autenticada; el listado solo expone ONGs con estado ACTIVE.
@RestController
@RequestMapping("/api/ngos")
@RequiredArgsConstructor
public class NgosController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll() {
        return new ApiResponse<>(
                true,
                "ONGs encontradas",
                userService.findAllActiveNgos()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findById(@PathVariable UUID id) {
        return new ApiResponse<>(
                true,
                "ONG encontrada",
                userService.findActiveNgoById(id)
        );
    }
}
