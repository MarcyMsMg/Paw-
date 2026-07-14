package com.paw.adoptions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.paw.adoptions.domain.AdoptionFormTemplate;
import com.paw.adoptions.domain.FormFieldType;
import com.paw.adoptions.dto.FormFieldRequest;
import com.paw.adoptions.dto.FormTemplateRequest;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AdoptionFormTemplateRepository;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;

/**
 * Cubre validateFields() (llamado desde create/update), que AdoptionFormServiceTest no ejercita:
 * opciones duplicadas, minimo de opciones y limite de campos personalizados.
 */
@ExtendWith(MockitoExtension.class)
class AdoptionFormServiceValidationTest {

    @Mock
    private AdoptionFormTemplateRepository templateRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private UserAccessValidator userAccessValidator;

    private AdoptionFormService formService;
    private AuthenticatedUser ngoUser;

    @BeforeEach
    void setUp() {
        formService = new AdoptionFormService(templateRepository, animalRepository, currentUserProvider, userAccessValidator);
        ReflectionTestUtils.setField(formService, "maxCustomFields", 15);
        ngoUser = new AuthenticatedUser(UUID.randomUUID(), "ngo@test.local", UserRole.NGO);
    }

    private static FormFieldRequest choiceField(FormFieldType type, List<String> options) {
        return new FormFieldRequest("Preferencia", type, false, null, options);
    }

    @Test
    void create_debeRechazar_cuandoUnaPreguntaDeSeleccionTieneOpcionesDuplicadas() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(ngoUser);
        FormTemplateRequest request = new FormTemplateRequest(
                "Plantilla perros grandes", null, true,
                List.of(choiceField(FormFieldType.SINGLE_CHOICE, List.of("Si", "No", "Si")))
        );

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> formService.create(request));

        // Assert
        assertTrue(exception.getMessage().contains("duplicate"));
        verify(templateRepository, never()).save(any());
    }

    @Test
    void create_debeRechazar_cuandoUnaPreguntaDeSeleccionTieneMenosDeDosOpciones() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(ngoUser);
        FormTemplateRequest request = new FormTemplateRequest(
                "Plantilla gatos", null, true,
                List.of(choiceField(FormFieldType.MULTIPLE_CHOICE, List.of("Solo una")))
        );

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> formService.create(request));

        // Assert
        assertTrue(exception.getMessage().contains("at least two"));
        verify(templateRepository, never()).save(any());
    }

    @Test
    void create_debeRechazar_cuandoSuperaElMaximoDeCamposPersonalizados() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(ngoUser);
        ReflectionTestUtils.setField(formService, "maxCustomFields", 2);
        FormFieldRequest textField = new FormFieldRequest("Pregunta", FormFieldType.SHORT_TEXT, false, null, List.of());
        FormTemplateRequest request = new FormTemplateRequest(
                "Plantilla con demasiados campos", null, true,
                List.of(textField, textField, textField)
        );

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> formService.create(request));

        // Assert
        assertTrue(exception.getMessage().contains("at most 2"));
        verify(templateRepository, never()).save(any());
    }

    @Test
    void create_debeGuardarPlantilla_cuandoOpcionesSonValidasYUnicas() {
        // Arrange
        when(currentUserProvider.get()).thenReturn(ngoUser);
        when(templateRepository.save(any(AdoptionFormTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FormTemplateRequest request = new FormTemplateRequest(
                "Plantilla perros grandes", null, true,
                List.of(choiceField(FormFieldType.SINGLE_CHOICE, List.of("Si", "No")))
        );

        // Act
        var response = formService.create(request);

        // Assert
        assertEquals("Plantilla perros grandes", response.name());
        assertEquals(1, response.fields().size());
        verify(templateRepository).save(any(AdoptionFormTemplate.class));
    }
}
