package com.paw.users.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.paw.users.dto.request.RejectNgoRequest;
import com.paw.users.dto.response.ApiResponse;
import com.paw.users.dto.response.NgoRegistrationRequestResponse;
import com.paw.users.service.AdminNgoRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/ngo-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNgoRequestController {

    private final AdminNgoRequestService adminNgoRequestService;

    @GetMapping
    public ApiResponse<List<NgoRegistrationRequestResponse>> findAll() {
        return new ApiResponse<>(
                true,
                "Solicitudes de registro de ONG encontradas",
                adminNgoRequestService.findAll()
        );
    }

    @GetMapping("/pending")
    public ApiResponse<List<NgoRegistrationRequestResponse>> findPending() {
        return new ApiResponse<>(
                true,
                "Solicitudes de registro de ONG pendientes encontradas",
                adminNgoRequestService.findPending()
        );
    }

    @PatchMapping("/{requestId}/approve")
    public ApiResponse<NgoRegistrationRequestResponse> approve(
            @PathVariable UUID requestId
    ) {
        return new ApiResponse<>(
                true,
                "Solicitud de registro de ONG aprobada correctamente",
                adminNgoRequestService.approve(requestId)
        );
    }

    @PatchMapping("/{requestId}/reject")
    public ApiResponse<NgoRegistrationRequestResponse> reject(
            @PathVariable UUID requestId,
            @Valid @RequestBody RejectNgoRequest solicitudRechazo
    ) {
        return new ApiResponse<>(
                true,
                "Solicitud de registro de ONG rechazada correctamente",
                adminNgoRequestService.reject(requestId, solicitudRechazo)
        );
    }
}
