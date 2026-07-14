package com.paw.users.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.paw.users.client.NotificationsClient;
import com.paw.users.dto.request.LoginRequest;
import com.paw.users.dto.request.NaturalPersonRegisterRequest;
import com.paw.users.dto.request.NgoRegisterRequest;
import com.paw.users.dto.response.AuthResponse;
import com.paw.users.dto.response.NgoRegistrationRequestResponse;
import com.paw.users.dto.response.UserResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.NgoRegistrationStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.model.User;
import com.paw.users.model.NgoRegistrationRequest;
import com.paw.users.repository.NgoRegistrationRequestRepository;
import com.paw.users.repository.UserRepository;
import com.paw.users.security.AuthTokenManager;
import com.paw.users.service.AuthService;

import jakarta.transaction.Transactional;

import java.time.Year;

import com.paw.users.exception.AccountNotActiveException;
import com.paw.users.exception.EmailAlreadyExistsException;
import com.paw.users.exception.InvalidCredentialsException;
import com.paw.users.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final NgoRegistrationRequestRepository ngoRegistrationRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenManager authTokenManager;
    private final NotificationsClient notificationsClient;

    @Override
    @Transactional
    public AuthResponse registerNaturalPerson(NaturalPersonRegisterRequest solicitud) {
        validarCorreo(solicitud.email());
        validarContrasena(solicitud.password());

        User usuario = User.builder()
                .firstName(solicitud.firstName().trim())
                .lastName(solicitud.lastName().trim())
                .email(normalizarCorreo(solicitud.email()))
                .password(passwordEncoder.encode(solicitud.password()))
                .profileImageUrl(solicitud.profileImageUrl())
                .role(UserRole.NATURAL_PERSON)
                .status(AccountStatus.ACTIVE)
                .build();

        User usuarioGuardado = userRepository.save(usuario);

        String token = authTokenManager.generateToken(usuarioGuardado);

        return new AuthResponse(token, toUserResponse(usuarioGuardado));
    }

    @Override
    @Transactional
    public NgoRegistrationRequestResponse requestNgoRegistration(NgoRegisterRequest solicitud) {
        validarCorreo(solicitud.email());
        validarContrasena(solicitud.password());
        validarAnioFundacion(solicitud.foundationYear());

        User usuario = User.builder()
                .email(normalizarCorreo(solicitud.email()))
                .password(passwordEncoder.encode(solicitud.password()))
                .profileImageUrl(solicitud.profileImageUrl())
                .ngoName(solicitud.ngoName().trim())
                .role(UserRole.NGO)
                .status(AccountStatus.PENDING)
                .build();

        User usuarioGuardado = userRepository.save(usuario);

        NgoRegistrationRequest solicitudOng = NgoRegistrationRequest.builder()
                .user(usuarioGuardado)
                .ngoName(solicitud.ngoName().trim())
                .description(solicitud.description().trim())
                .coverImageUrl(solicitud.coverImageUrl())
                .constitutionActUrl(solicitud.constitutionActUrl().trim())
                .location(solicitud.location().trim())
                .foundationYear(solicitud.foundationYear())
                .rescuedAnimalsCount(solicitud.rescuedAnimalsCount())
                .volunteersCount(solicitud.volunteersCount())
                .status(NgoRegistrationStatus.PENDING)
                .build();

        NgoRegistrationRequest solicitudGuardada = ngoRegistrationRequestRepository.save(solicitudOng);
        notifyAdminsAboutNgoRequest(solicitudGuardada);

        return toNgoRequestResponse(solicitudGuardada);
    }

    @Override
    public AuthResponse login(LoginRequest solicitud) {
        User usuario = userRepository.findByEmailIgnoreCase(normalizarCorreo(solicitud.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Correo o contraseÃ±a invÃ¡lidos"));

        boolean coincideContrasena = passwordEncoder.matches(solicitud.password(), usuario.getPassword());

        if (!coincideContrasena) {
            throw new InvalidCredentialsException("Correo o contraseÃ±a invÃ¡lidos");
        }

        if (usuario.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("La cuenta no estÃ¡ activa. Estado actual: " + usuario.getStatus());
        }

        String token = authTokenManager.generateToken(usuario);

        return new AuthResponse(token, toUserResponse(usuario));
    }


    private void notifyAdminsAboutNgoRequest(NgoRegistrationRequest solicitud) {
        userRepository.findByRole(UserRole.ADMIN).stream()
                .filter(admin -> admin.getStatus() == AccountStatus.ACTIVE)
                .forEach(admin -> notificationsClient.send(
                        "users.ngo.registration_created." + solicitud.getId() + "." + admin.getId(),
                        "NGO_REGISTRATION_CREATED",
                        admin.getId(),
                        UserRole.ADMIN,
                        "Nueva solicitud de ONG",
                        solicitud.getNgoName() + " envio una solicitud de registro.",
                        "NGO_REQUEST",
                        solicitud.getId(),
                        "/admin/solicitudes-ong",
                        "{\"ngoUserId\":\"" + solicitud.getUser().getId() + "\"}"
                ));
    }
    private String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }

    private void validarCorreo(String correo) {
        if (userRepository.existsByEmailIgnoreCase(normalizarCorreo(correo))) {
            throw new EmailAlreadyExistsException("El correo ya estÃ¡ registrado");
        }
    }

    private void validarContrasena(String password) {
        if (!password.equals(password.trim())) {
            throw new InvalidRequestException("La contraseña no puede empezar ni terminar con espacios");
        }
    }

    private void validarAnioFundacion(Integer foundationYear) {
        if (foundationYear != null && foundationYear > Year.now().getValue()) {
            throw new InvalidRequestException("El año de fundación no puede ser posterior al año actual");
        }
    }

    private UserResponse toUserResponse(User usuario) {
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

    private NgoRegistrationRequestResponse toNgoRequestResponse(NgoRegistrationRequest solicitud) {
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
