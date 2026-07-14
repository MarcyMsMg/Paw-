package com.paw.users.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paw.users.client.NotificationsClient;
import com.paw.users.dto.request.RejectNgoRequest;
import com.paw.users.dto.response.NgoRegistrationRequestResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.NgoRegistrationStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.ResourceNotFoundException;
import com.paw.users.model.NgoRegistrationRequest;
import com.paw.users.model.User;
import com.paw.users.repository.NgoRegistrationRequestRepository;
import com.paw.users.service.AdminNgoRequestService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminNgoRequestServiceImpl implements AdminNgoRequestService {

    private final NgoRegistrationRequestRepository ngoRegistrationRequestRepository;
    private final NotificationsClient notificationsClient;

    @Override
    public List<NgoRegistrationRequestResponse> findAll() {
        return ngoRegistrationRequestRepository.findAllWithUser()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<NgoRegistrationRequestResponse> findPending() {
        return ngoRegistrationRequestRepository.findByStatusWithUser(NgoRegistrationStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NgoRegistrationRequestResponse approve(UUID requestId) {
        NgoRegistrationRequest solicitud = findById(requestId);

        solicitud.setStatus(NgoRegistrationStatus.APPROVED);
        solicitud.setReviewedAt(LocalDateTime.now());
        solicitud.setRejectionReason(null);

        // Al aprobar, copiamos los datos descriptivos de la solicitud al perfil de la ONG.
        // La solicitud queda como histórico inmutable; el perfil del usuario pasa a ser
        // editable por la propia ONG desde su pantalla de perfil.
        User usuario = solicitud.getUser();
        usuario.setStatus(AccountStatus.ACTIVE);
        usuario.setDescription(solicitud.getDescription());
        usuario.setCoverImageUrl(solicitud.getCoverImageUrl());
        usuario.setLocation(solicitud.getLocation());
        usuario.setFoundationYear(solicitud.getFoundationYear());
        usuario.setRescuedAnimalsCount(solicitud.getRescuedAnimalsCount());
        usuario.setVolunteersCount(solicitud.getVolunteersCount());

        notifyNgoRegistrationDecision(solicitud, true);

        return toResponse(solicitud);
    }

    @Override
    @Transactional
    public NgoRegistrationRequestResponse reject(UUID requestId, RejectNgoRequest solicitudRechazo) {
        NgoRegistrationRequest solicitud = findById(requestId);

        solicitud.setStatus(NgoRegistrationStatus.REJECTED);
        solicitud.setReviewedAt(LocalDateTime.now());
        solicitud.setRejectionReason(solicitudRechazo.reason());

        User usuario = solicitud.getUser();
        usuario.setStatus(AccountStatus.REJECTED);

        notifyNgoRegistrationDecision(solicitud, false);

        return toResponse(solicitud);
    }

    private void notifyNgoRegistrationDecision(NgoRegistrationRequest solicitud, boolean approved) {
        User usuario = solicitud.getUser();

        notificationsClient.send(
                "users.ngo.registration_" + (approved ? "approved" : "rejected") + "." + solicitud.getId(),
                approved ? "NGO_REGISTRATION_APPROVED" : "NGO_REGISTRATION_REJECTED",
                usuario.getId(),
                UserRole.NGO,
                approved ? "Solicitud ONG aprobada" : "Solicitud ONG rechazada",
                approved ? "Tu ONG ya fue aprobada en Paw+." : "Tu solicitud de ONG fue rechazada.",
                "NGO_REQUEST",
                solicitud.getId(),
                approved ? "/ong/dashboard" : "/login",
                buildMetadata(solicitud)
        );
    }

    private String buildMetadata(NgoRegistrationRequest solicitud) {
        String reason = solicitud.getRejectionReason() == null
                ? ""
                : solicitud.getRejectionReason().replace("\"", "'");

        return "{\"reason\":\"" + reason + "\"}";
    }

    private NgoRegistrationRequest findById(UUID requestId) {
        return ngoRegistrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de registro de ONG no encontrada"));
    }

    private NgoRegistrationRequestResponse toResponse(NgoRegistrationRequest solicitud) {
        return new NgoRegistrationRequestResponse(
                solicitud.getId(),
                solicitud.getUser().getId(),
                solicitud.getNgoName(),
                solicitud.getUser().getEmail(),
                solicitud.getDescription(),
                solicitud.getConstitutionActUrl(),
                solicitud.getLocation(),
                solicitud.getFoundationYear(),
                solicitud.getRescuedAnimalsCount(),
                solicitud.getVolunteersCount(),
                solicitud.getStatus(),
                solicitud.getRejectionReason(),
                solicitud.getCreatedAt(),
                solicitud.getReviewedAt()
        );
    }
}