package com.paw.adoptions.service;

import com.paw.adoptions.domain.*;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AdoptionFormTemplateRepository;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionFormServiceTest {

    @Mock AdoptionFormTemplateRepository templateRepository;
    @Mock AnimalRepository animalRepository;
    @Mock CurrentUserProvider currentUserProvider;
    @Mock UserAccessValidator userAccessValidator;

    private AdoptionFormService formService;
    private Animal animal;
    private AdoptionFormTemplate template;

    @BeforeEach
    void setUp() {
        formService = new AdoptionFormService(
                templateRepository,
                animalRepository,
                currentUserProvider,
                userAccessValidator
        );
        ReflectionTestUtils.setField(formService, "maxCustomFields", 15);

        UUID ngoId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        template = new AdoptionFormTemplate();
        template.setId(templateId);
        template.setNgoId(ngoId);
        template.setRevision(3);

        AdoptionFormField field = new AdoptionFormField();
        field.setTemplate(template);
        field.setFieldKey("custom_1");
        field.setLabel("Do you have a fenced yard?");
        field.setType(FormFieldType.BOOLEAN);
        field.setRequired(true);
        field.setDisplayOrder(0);
        template.getFields().add(field);

        animal = new Animal();
        animal.setNgoId(ngoId);
        animal.setFormTemplateId(templateId);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
    }

    @Test
    void shouldRejectMissingRequiredCustomAnswer() {
        AdoptionApplication application = new AdoptionApplication();

        ApiException exception = assertThrows(
                ApiException.class,
                () -> formService.validateAndAttachAnswers(animal, Map.of(), application)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
        assertTrue(exception.getMessage().contains("fenced yard"));
    }

    @Test
    void shouldStoreTemplateAndAnswerSnapshots() {
        AdoptionApplication application = new AdoptionApplication();

        formService.validateAndAttachAnswers(
                animal,
                Map.of("custom_1", "true"),
                application
        );

        assertEquals(template.getId(), application.getFormTemplateId());
        assertEquals(3, application.getFormTemplateRevision());
        assertEquals(1, application.getCustomAnswers().size());
        AdoptionApplicationAnswer answer = application.getCustomAnswers().getFirst();
        assertEquals("Do you have a fenced yard?", answer.getLabelSnapshot());
        assertEquals("true", answer.getAnswerValue());
        assertSame(application, answer.getApplication());
    }
}
