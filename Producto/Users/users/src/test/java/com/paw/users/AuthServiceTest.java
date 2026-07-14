package com.paw.users;

import com.paw.users.dto.request.LoginRequest;
import com.paw.users.dto.request.NaturalPersonRegisterRequest;
import com.paw.users.dto.response.AuthResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.exception.AccountNotActiveException;
import com.paw.users.exception.EmailAlreadyExistsException;
import com.paw.users.exception.InvalidCredentialsException;
import com.paw.users.model.User;
import com.paw.users.repository.NgoRegistrationRequestRepository;
import com.paw.users.repository.UserRepository;
import com.paw.users.security.AuthTokenManager;
import com.paw.users.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CP_AUTH - Pruebas de Autenticacion y Registro")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NgoRegistrationRequestRepository ngoRegistrationRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenManager authTokenManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User usuarioActivo;
    private User usuarioInactivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = User.builder()
                .id(UUID.randomUUID())
                .email("maria@test.com")
                .password("$2a$10$hasheado")
                .firstName("Maria")
                .lastName("Lopez")
                .role(UserRole.NATURAL_PERSON)
                .status(AccountStatus.ACTIVE)
                .build();

        usuarioInactivo = User.builder()
                .id(UUID.randomUUID())
                .email("inactivo@test.com")
                .password("$2a$10$hasheado")
                .role(UserRole.NATURAL_PERSON)
                .status(AccountStatus.DISABLED)
                .build();
    }

    // ─── LOGIN ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP_AUTH_01 - Login exitoso con credenciales correctas")
    void login_credencialesCorrectas_retornaTokenYDatosUsuario() {
        when(userRepository.findByEmailIgnoreCase("maria@test.com"))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("Test1234!", "$2a$10$hasheado"))
                .thenReturn(true);
        when(authTokenManager.generateToken(usuarioActivo))
                .thenReturn("jwt.token.generado");

        AuthResponse respuesta = authService.login(new LoginRequest("maria@test.com", "Test1234!"));

        assertNotNull(respuesta);
        assertEquals("jwt.token.generado", respuesta.token());
        assertEquals("maria@test.com", respuesta.user().email());
        verify(authTokenManager, times(1)).generateToken(usuarioActivo);
    }

    @Test
    @DisplayName("CP_AUTH_04 - Login falla con contrasena incorrecta")
    void login_contrasennaIncorrecta_lanzaInvalidCredentialsException() {
        when(userRepository.findByEmailIgnoreCase("maria@test.com"))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("clavemal", "$2a$10$hasheado"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login(new LoginRequest("maria@test.com", "clavemal"))
        );

        verify(authTokenManager, never()).generateToken(any());
    }

    @Test
    @DisplayName("CP_AUTH_05 - Login falla con email no registrado")
    void login_emailNoRegistrado_lanzaInvalidCredentialsException() {
        when(userRepository.findByEmailIgnoreCase("noexiste@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login(new LoginRequest("noexiste@test.com", "cualquier"))
        );

        verify(authTokenManager, never()).generateToken(any());
    }

    @Test
    @DisplayName("CP_AUTH_06 - Login falla con cuenta inactiva")
    void login_cuentaInactiva_lanzaAccountNotActiveException() {
        when(userRepository.findByEmailIgnoreCase("inactivo@test.com"))
                .thenReturn(Optional.of(usuarioInactivo));
        when(passwordEncoder.matches("Test1234!", "$2a$10$hasheado"))
                .thenReturn(true);

        assertThrows(AccountNotActiveException.class, () ->
                authService.login(new LoginRequest("inactivo@test.com", "Test1234!"))
        );

        verify(authTokenManager, never()).generateToken(any());
    }

    // ─── REGISTRO ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CP_REG_01 - Registro exitoso de Persona Natural")
    void registrarPersonaNatural_datosValidos_retornaTokenYUsuario() {
        when(userRepository.existsByEmailIgnoreCase("nueva@test.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("Test1234!"))
                .thenReturn("$2a$10$hasheado");
        when(userRepository.save(any(User.class)))
                .thenReturn(usuarioActivo);
        when(authTokenManager.generateToken(any(User.class)))
                .thenReturn("jwt.token.nuevo");

        NaturalPersonRegisterRequest solicitud = new NaturalPersonRegisterRequest(
                "Ana", "Lopez", "nueva@test.com", "Test1234!", null
        );

        AuthResponse respuesta = authService.registerNaturalPerson(solicitud);

        assertNotNull(respuesta);
        assertNotNull(respuesta.token());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("CP_REG_02 - Registro falla con email ya registrado")
    void registrarPersonaNatural_emailDuplicado_lanzaEmailAlreadyExistsException() {
        when(userRepository.existsByEmailIgnoreCase("maria@test.com"))
                .thenReturn(true);

        NaturalPersonRegisterRequest solicitud = new NaturalPersonRegisterRequest(
                "Ana", "Lopez", "maria@test.com", "Test1234!", null
        );

        assertThrows(EmailAlreadyExistsException.class, () ->
                authService.registerNaturalPerson(solicitud)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP_SEG_03 - La contrasena se guarda cifrada con BCrypt")
    void registrarPersonaNatural_contrasenaGuardadaCifrada_nuncaEnTextoPlano() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Test1234!")).thenReturn("$2a$10$hashBCrypt");
        when(userRepository.save(any(User.class))).thenReturn(usuarioActivo);
        when(authTokenManager.generateToken(any())).thenReturn("token");

        NaturalPersonRegisterRequest solicitud = new NaturalPersonRegisterRequest(
                "Ana", "Lopez", "ana@test.com", "Test1234!", null
        );

        authService.registerNaturalPerson(solicitud);

        verify(passwordEncoder, times(1)).encode("Test1234!");
        verify(userRepository).save(argThat(usuario ->
                usuario.getPassword().startsWith("$2a$") &&
                !usuario.getPassword().equals("Test1234!")
        ));
    }
}
