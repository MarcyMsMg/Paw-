package com.paw.adoptions.service;

import com.paw.adoptions.domain.*;
import com.paw.adoptions.dto.*;
import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.repository.AdoptionFormTemplateRepository;
import com.paw.adoptions.repository.AnimalRepository;
import com.paw.adoptions.security.AuthenticatedUser;
import com.paw.adoptions.security.CurrentUserProvider;
import com.paw.adoptions.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdoptionFormService {

    private static final List<FormFieldResponse> SYSTEM_FIELDS = List.of(
            system("fullName", "Full name", FormFieldType.SHORT_TEXT, true, 0),
            system("email", "Email", FormFieldType.EMAIL, true, 1),
            system("phone", "Phone", FormFieldType.PHONE, true, 2),
            system("address", "Address or district", FormFieldType.SHORT_TEXT, true, 3),
            system("housingType", "Housing type", FormFieldType.SHORT_TEXT, true, 4),
            system("otherAnimals", "Other animals at home", FormFieldType.SHORT_TEXT, true, 5),
            system("motivation", "Why do you want to adopt?", FormFieldType.LONG_TEXT, true, 6),
            system("availability", "Availability for an interview", FormFieldType.SHORT_TEXT, false, 7),
            system("previousExperience", "Previous experience with animals", FormFieldType.LONG_TEXT, false, 8)
    );

    private final AdoptionFormTemplateRepository templateRepository;
    private final AnimalRepository animalRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserAccessValidator userAccessValidator;

    @Value("${paw.adoptions.forms.max-custom-fields:15}")
    private int maxCustomFields;

    @Transactional(readOnly = true)
    public List<FormTemplateResponse> listOwned() {
        AuthenticatedUser ngo = requireActiveNgo();
        return templateRepository.findByNgoIdOrderByUpdatedAtDesc(ngo.id()).stream()
                .map(this::toTemplateResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FormTemplateResponse getOwned(UUID templateId) {
        AuthenticatedUser ngo = requireActiveNgo();
        return toTemplateResponse(findOwned(templateId, ngo.id()));
    }

    @Transactional
    public FormTemplateResponse create(FormTemplateRequest request) {
        AuthenticatedUser ngo = requireActiveNgo();
        validateFields(request.fields());

        AdoptionFormTemplate template = new AdoptionFormTemplate();
        template.setNgoId(ngo.id());
        applyRequest(template, request, false);
        return toTemplateResponse(templateRepository.save(template));
    }

    @Transactional
    public FormTemplateResponse update(UUID templateId, FormTemplateRequest request) {
        AuthenticatedUser ngo = requireActiveNgo();
        AdoptionFormTemplate template = findOwned(templateId, ngo.id());
        validateFields(request.fields());
        applyRequest(template, request, true);
        return toTemplateResponse(templateRepository.save(template));
    }

    @Transactional
    public void deactivate(UUID templateId) {
        AuthenticatedUser ngo = requireActiveNgo();
        AdoptionFormTemplate template = findOwned(templateId, ngo.id());
        template.setActive(false);
        template.setRevision(template.getRevision() + 1);
        templateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public AdoptionFormResponse getForAnimal(UUID animalId) {
        Animal animal = animalRepository.findById(animalId)
                .filter(Animal::isPublished)
                .filter(item -> item.getStatus() != AnimalStatus.RETIRED)
                .orElseThrow(() -> ApiException.notFound("Animal not found"));

        if (animal.getFormTemplateId() == null) {
            return standardForm();
        }

        AdoptionFormTemplate template = templateRepository.findById(animal.getFormTemplateId())
                .filter(item -> item.getNgoId().equals(animal.getNgoId()))
                .orElseThrow(() -> ApiException.notFound("Adoption form not found"));
        return toAdoptionForm(template);
    }

    @Transactional(readOnly = true)
    public void requireAssignableTemplate(UUID templateId, UUID ngoId) {
        if (templateId == null) {
            return;
        }
        templateRepository.findByIdAndNgoIdAndActiveTrue(templateId, ngoId)
                .orElseThrow(() -> ApiException.badRequest("The selected adoption form is not active or does not belong to the NGO"));
    }

    @Transactional(readOnly = true)
    public void validateAndAttachAnswers(
            Animal animal,
            Map<String, String> submittedAnswers,
            AdoptionApplication application
    ) {
        Map<String, String> answers = submittedAnswers == null ? Map.of() : submittedAnswers;
        if (animal.getFormTemplateId() == null) {
            if (!answers.isEmpty()) {
                throw ApiException.badRequest("This animal does not have custom adoption questions");
            }
            return;
        }

        AdoptionFormTemplate template = templateRepository.findById(animal.getFormTemplateId())
                .filter(item -> item.getNgoId().equals(animal.getNgoId()))
                .orElseThrow(() -> ApiException.badRequest("The adoption form assigned to this animal is unavailable"));

        Set<String> validKeys = new HashSet<>();
        for (AdoptionFormField field : template.getFields()) {
            validKeys.add(field.getFieldKey());
            String value = trimToNull(answers.get(field.getFieldKey()));
            if (field.isRequired() && value == null) {
                throw ApiException.badRequest("A response is required for: " + field.getLabel());
            }
            if (value != null) {
                validateAnswer(field, value);
                AdoptionApplicationAnswer answer = new AdoptionApplicationAnswer();
                answer.setFieldKey(field.getFieldKey());
                answer.setLabelSnapshot(field.getLabel());
                answer.setTypeSnapshot(field.getType());
                answer.setAnswerValue(value);
                answer.setDisplayOrder(field.getDisplayOrder());
                application.addCustomAnswer(answer);
            }
        }

        if (answers.keySet().stream().anyMatch(key -> !validKeys.contains(key))) {
            throw ApiException.badRequest("The application contains unknown form fields");
        }
        application.setFormTemplateId(template.getId());
        application.setFormTemplateRevision(template.getRevision());
    }

    private void applyRequest(AdoptionFormTemplate template, FormTemplateRequest request, boolean update) {
        template.setName(request.name().trim());
        template.setDescription(trimToNull(request.description()));
        template.setActive(request.active());
        if (update) {
            template.setRevision(template.getRevision() + 1);
        }

        List<AdoptionFormField> fields = new ArrayList<>();
        for (int index = 0; index < request.fields().size(); index++) {
            FormFieldRequest source = request.fields().get(index);
            AdoptionFormField field = new AdoptionFormField();
            field.setFieldKey("custom_" + (index + 1));
            field.setLabel(source.label().trim());
            field.setType(source.type());
            field.setRequired(source.required());
            field.setPlaceholder(trimToNull(source.placeholder()));
            field.setDisplayOrder(index);
            field.setOptions(cleanOptions(source.options()));
            fields.add(field);
        }
        template.replaceFields(fields);
    }

    private void validateFields(List<FormFieldRequest> fields) {
        if (fields.size() > maxCustomFields) {
            throw ApiException.badRequest("A form can contain at most " + maxCustomFields + " custom fields");
        }
        for (FormFieldRequest field : fields) {
            boolean choice = field.type() == FormFieldType.SINGLE_CHOICE
                    || field.type() == FormFieldType.MULTIPLE_CHOICE;
            List<String> rawOptions = field.options() == null
                    ? List.of()
                    : field.options().stream().map(String::trim).filter(value -> !value.isBlank()).toList();
            List<String> options = cleanOptions(field.options());
            if (choice && rawOptions.size() != options.size()) {
                throw ApiException.badRequest("Choice fields cannot have duplicate options");
            }
            if (choice && options.size() < 2) {
                throw ApiException.badRequest("Choice fields require at least two different options");
            }
            if (!choice && !options.isEmpty()) {
                throw ApiException.badRequest("Only choice fields can define options");
            }
        }
    }

    private void validateAnswer(AdoptionFormField field, String value) {
        switch (field.getType()) {
            case NUMBER -> {
                try {
                    new BigDecimal(value);
                } catch (NumberFormatException exception) {
                    throw ApiException.badRequest("A numeric response is required for: " + field.getLabel());
                }
            }
            case EMAIL -> {
                if (!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    throw ApiException.badRequest("A valid email is required for: " + field.getLabel());
                }
            }
            case BOOLEAN -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw ApiException.badRequest("A yes/no response is required for: " + field.getLabel());
                }
            }
            case SINGLE_CHOICE -> requireAllowedOptions(field, List.of(value));
            case MULTIPLE_CHOICE -> requireAllowedOptions(
                    field,
                    Arrays.stream(value.split("\\n")).map(String::trim).filter(item -> !item.isBlank()).toList()
            );
            default -> {
                // Length is already bounded by the request DTO.
            }
        }
    }

    private void requireAllowedOptions(AdoptionFormField field, List<String> values) {
        if (values.isEmpty() || values.stream().anyMatch(value -> !field.getOptions().contains(value))) {
            throw ApiException.badRequest("An invalid option was submitted for: " + field.getLabel());
        }
    }

    private FormTemplateResponse toTemplateResponse(AdoptionFormTemplate template) {
        return new FormTemplateResponse(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.isActive(),
                template.getRevision(),
                maxCustomFields,
                animalRepository.countByFormTemplateId(template.getId()),
                customFieldResponses(template),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    private AdoptionFormResponse toAdoptionForm(AdoptionFormTemplate template) {
        List<FormFieldResponse> fields = new ArrayList<>(SYSTEM_FIELDS);
        fields.addAll(customFieldResponses(template));
        return new AdoptionFormResponse(
                template.getId(),
                template.getName(),
                template.getRevision(),
                maxCustomFields,
                fields
        );
    }

    private AdoptionFormResponse standardForm() {
        return new AdoptionFormResponse(null, "Standard adoption form", 0, maxCustomFields, SYSTEM_FIELDS);
    }

    private List<FormFieldResponse> customFieldResponses(AdoptionFormTemplate template) {
        return template.getFields().stream()
                .map(field -> new FormFieldResponse(
                        field.getFieldKey(),
                        field.getLabel(),
                        field.getType(),
                        field.isRequired(),
                        false,
                        field.getPlaceholder(),
                        List.copyOf(field.getOptions()),
                        SYSTEM_FIELDS.size() + field.getDisplayOrder()
                ))
                .toList();
    }

    private AdoptionFormTemplate findOwned(UUID templateId, UUID ngoId) {
        return templateRepository.findByIdAndNgoId(templateId, ngoId)
                .orElseThrow(() -> ApiException.notFound("Adoption form template not found"));
    }

    private AuthenticatedUser requireActiveNgo() {
        AuthenticatedUser user = currentUserProvider.get();
        if (user.role() != UserRole.NGO) {
            throw ApiException.forbidden("This operation requires role NGO");
        }
        userAccessValidator.requireActive(user);
        return user;
    }

    private List<String> cleanOptions(List<String> options) {
        if (options == null) {
            return new ArrayList<>();
        }
        return options.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static FormFieldResponse system(
            String key,
            String label,
            FormFieldType type,
            boolean required,
            int order
    ) {
        return new FormFieldResponse(key, label, type, required, true, null, List.of(), order);
    }
}
