package com.paw.users.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paw.users.client.NotificationsClient;
import com.paw.users.dto.request.UpdateUserStatusRequest;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.InvalidRequestException;
import com.paw.users.exception.ResourceNotFoundException;
import com.paw.users.model.User;
import com.paw.users.repository.UserRepository;
import com.paw.users.service.AdminUserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final NotificationsClient notificationsClient;

    @Override
    public List<UserResponse> findAll(UserRole role) {
        List<User> usuarios = (role == null)
                ? userRepository.findAll()
                : userRepository.findByRole(role);

        return usuarios.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse updateStatus(UUID userId, UpdateUserStatusRequest solicitud) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        AccountStatus current = usuario.getStatus();
        AccountStatus target = solicitud.status();

        // Este endpoint solo activa/desactiva cuentas ya aprobadas. PENDING y REJECTED
        // se gestionan exclusivamente a través del flujo de solicitudes de ONG.
        boolean transicionValida = (current == AccountStatus.ACTIVE && target == AccountStatus.DISABLED)
                || (current == AccountStatus.DISABLED && target == AccountStatus.ACTIVE);
        if (!transicionValida) {
            throw new InvalidRequestException(
                    "Transición de estado no permitida: " + current + " -> " + target
            );
        }

        usuario.setStatus(target);

        return toResponse(usuario);
    }

    private UserResponse toResponse(User usuario) {
        return new UserResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getFirstName(),
                usuario.getLastName(),
                usuario.getNgoName(),
                usuario.getProfileImageUrl(),
                usuario.getDescription(),
                usuario.getCoverImageUrl(),
                usuario.getLocation(),
                usuario.getFoundationYear(),
                usuario.getRescuedAnimalsCount(),
                usuario.getVolunteersCount(),
                usuario.getRole(),
                usuario.getStatus(),
                usuario.getCreatedAt()
        );
    }
}
