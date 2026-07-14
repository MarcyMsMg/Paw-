package com.paw.users.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.users.dto.request.LoginRequest;
import com.paw.users.dto.request.NaturalPersonRegisterRequest;
import com.paw.users.dto.request.NgoRegisterRequest;
import com.paw.users.dto.response.ApiResponse;
import com.paw.users.dto.response.AuthResponse;
import com.paw.users.dto.response.NgoRegistrationRequestResponse;
import com.paw.users.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/natural-person")
    public ApiResponse<AuthResponse> registerNaturalPerson(
            @Valid @RequestBody NaturalPersonRegisterRequest solicitud
    ) {
        return new ApiResponse<>(
                true,
                "Persona natural registrada correctamente",
                authService.registerNaturalPerson(solicitud)
        );
    }

    @PostMapping("/register/ngo-request")
    public ApiResponse<NgoRegistrationRequestResponse> requestNgoRegistration(
            @Valid @RequestBody NgoRegisterRequest solicitud
    ) {
        return new ApiResponse<>(
                true,
                "Solicitud de registro de ONG enviada correctamente. Un administrador la revisará.",
                authService.requestNgoRegistration(solicitud)
        );
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest solicitud
    ) {
        return new ApiResponse<>(
                true,
                "Inicio de sesión exitoso",
                authService.login(solicitud)
        );
    }
}