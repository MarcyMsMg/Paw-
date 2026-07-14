package com.paw.users.service.impl;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paw.users.dto.request.UpdateUserProfileRequest;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.dto.response.InternalUserAccessResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.InvalidRequestException;
import com.paw.users.exception.ResourceNotFoundException;
import com.paw.users.model.User;
import com.paw.users.repository.UserRepository;
import com.paw.users.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse findById(UUID userId) {
        return toResponse(buscarOLanzar(userId));
    }

    @Override
    public UserResponse findActiveNgoById(UUID userId) {
        User user = buscarOLanzar(userId);
        if (user.getRole() != UserRole.NGO || user.getStatus() != AccountStatus.ACTIVE) {
            throw new ResourceNotFoundException("ONG no encontrada");
        }
        return toResponse(user);
    }

    @Override
    public InternalUserAccessResponse findAccessById(UUID userId) {
        User user = buscarOLanzar(userId);
        return new InternalUserAccessResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getNgoName(),
                user.getLocation()
        );
    }

    @Override
    public List<UserResponse> findAllActiveNgos() {
        return userRepository.findByRole(UserRole.NGO).stream()
                .filter(usuario -> usuario.getStatus() == AccountStatus.ACTIVE)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateUserProfileRequest solicitud) {
        User usuario = buscarOLanzar(userId);

        if (solicitud.foundationYear() != null && solicitud.foundationYear() > Year.now().getValue()) {
            throw new InvalidRequestException("El año de fundación no puede ser posterior al año actual");
        }

        if (usuario.getRole() == UserRole.NGO) {
            if (solicitud.ngoName() != null) {
                usuario.setNgoName(solicitud.ngoName().trim());
            }
            if (solicitud.description() != null) {
                usuario.setDescription(solicitud.description().trim());
            }
            if (solicitud.coverImageUrl() != null) {
                usuario.setCoverImageUrl(solicitud.coverImageUrl());
            }
            if (solicitud.location() != null) {
                usuario.setLocation(solicitud.location().trim());
            }
            if (solicitud.foundationYear() != null) {
                usuario.setFoundationYear(solicitud.foundationYear());
            }
            if (solicitud.rescuedAnimalsCount() != null) {
                usuario.setRescuedAnimalsCount(solicitud.rescuedAnimalsCount());
            }
            if (solicitud.volunteersCount() != null) {
                usuario.setVolunteersCount(solicitud.volunteersCount());
            }
        } else {
            if (solicitud.firstName() != null) {
                usuario.setFirstName(solicitud.firstName().trim());
            }
            if (solicitud.lastName() != null) {
                usuario.setLastName(solicitud.lastName().trim());
            }
        }

        if (solicitud.profileImageUrl() != null) {
            usuario.setProfileImageUrl(solicitud.profileImageUrl());
        }

        return toResponse(usuario);
    }

    private User buscarOLanzar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
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
