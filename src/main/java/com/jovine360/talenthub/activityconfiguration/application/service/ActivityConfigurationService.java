package com.jovine360.talenthub.activityconfiguration.application.service;

import com.jovine360.talenthub.activityconfiguration.api.*;
import com.jovine360.talenthub.activityconfiguration.domain.event.ActivitySchemaPublishedEvent;
import com.jovine360.talenthub.activityconfiguration.domain.model.*;
import com.jovine360.talenthub.activityconfiguration.domain.repository.*;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Full activity configuration service: manages templates, schema versions and field definitions.
 */
@Service
@Transactional
public class ActivityConfigurationService implements ActivityConfigApi {

    private final ActivityTemplateRepository templateRepository;
    private final ActivityTypeRepository typeRepository;
    private final ActivitySchemaVersionRepository schemaVersionRepository;
    private final ActivityFieldDefinitionRepository fieldDefinitionRepository;
    private final ActivityFieldOptionRepository fieldOptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ActivityConfigurationService(
            ActivityTemplateRepository templateRepository,
            ActivityTypeRepository typeRepository,
            ActivitySchemaVersionRepository schemaVersionRepository,
            ActivityFieldDefinitionRepository fieldDefinitionRepository,
            ActivityFieldOptionRepository fieldOptionRepository,
            ApplicationEventPublisher eventPublisher) {
        this.templateRepository = templateRepository;
        this.typeRepository = typeRepository;
        this.schemaVersionRepository = schemaVersionRepository;
        this.fieldDefinitionRepository = fieldDefinitionRepository;
        this.fieldOptionRepository = fieldOptionRepository;
        this.eventPublisher = eventPublisher;
    }

    // ---- ActivityConfigApi ------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<ActivityTemplateDto> listActiveTemplates() {
        return templateRepository.findByActiveTrue().stream().map(this::toTemplateDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActivityTemplateDto> findByCode(String code) {
        return templateRepository.findByCode(code).map(this::toTemplateDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PublishedSchemaDto> getPublishedSchema(String templateId) {
        return schemaVersionRepository
                .findFirstByTemplateIdAndStatusOrderByVersionNumberDesc(templateId, SchemaStatus.PUBLISHED)
                .map(sv -> {
                    List<FieldDefinitionDto> fields = fieldDefinitionRepository
                            .findBySchemaVersionIdAndActiveTrueOrderByDisplayOrder(sv.getId())
                            .stream().map(this::toFieldDto).toList();
                    return new PublishedSchemaDto(sv.getId(), sv.getTemplateId(), sv.getVersionNumber(), fields);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FieldDefinitionDto> getFieldDefinition(String fieldId) {
        return fieldDefinitionRepository.findById(fieldId).map(this::toFieldDto);
    }

    // ---- Admin operations ------------------------------------------------

    public ActivityTemplate createTemplate(String activityTypeId, String sportId,
                                           String name, String description) {
        ActivityTemplate template = new ActivityTemplate();
        template.setActivityTypeId(activityTypeId);
        template.setSportId(sportId);
        template.setName(name);
        template.setDescription(description);
        template.setCode(name.toUpperCase().replaceAll("\\s+", "_"));
        template.setActive(true);
        return templateRepository.save(template);
    }

    public ActivityFieldDefinition addField(String schemaVersionId, String fieldCode,
                                             String fieldLabel, FieldType fieldType,
                                             boolean required, int displayOrder) {
        ActivitySchemaVersion version = schemaVersionRepository.findById(schemaVersionId)
                .orElseThrow(() -> new EntityNotFoundException("ActivitySchemaVersion", schemaVersionId));
        if (version.getStatus() != SchemaStatus.DRAFT) {
            throw new InvalidStateTransitionException(
                    "ActivitySchemaVersion", version.getStatus().name(), "ADD_FIELD");
        }
        ActivityFieldDefinition field = new ActivityFieldDefinition();
        field.setSchemaVersionId(schemaVersionId);
        field.setFieldCode(fieldCode);
        field.setFieldLabel(fieldLabel);
        field.setFieldType(fieldType);
        field.setRequired(required);
        field.setDisplayOrder(displayOrder);
        field.setActive(true);
        return fieldDefinitionRepository.save(field);
    }

    public ActivityFieldOption addFieldOption(String fieldDefinitionId,
                                               String optionValue, String displayLabel, int ordinal) {
        ActivityFieldOption option = new ActivityFieldOption();
        option.setFieldDefinitionId(fieldDefinitionId);
        option.setOptionValue(optionValue);
        option.setDisplayLabel(displayLabel);
        option.setOrdinal(ordinal);
        return fieldOptionRepository.save(option);
    }

    public ActivitySchemaVersion publishSchema(String schemaVersionId, String publishedBy) {
        ActivitySchemaVersion version = schemaVersionRepository.findById(schemaVersionId)
                .orElseThrow(() -> new EntityNotFoundException("ActivitySchemaVersion", schemaVersionId));
        if (version.getStatus() != SchemaStatus.DRAFT) {
            throw new InvalidStateTransitionException(
                    "ActivitySchemaVersion", version.getStatus().name(), "PUBLISHED");
        }
        version.setStatus(SchemaStatus.PUBLISHED);
        version.setPublishedAt(Instant.now());
        version.setPublishedBy(publishedBy);
        ActivitySchemaVersion saved = schemaVersionRepository.save(version);
        eventPublisher.publishEvent(
                new ActivitySchemaPublishedEvent(saved.getId(), saved.getTemplateId(), saved.getVersionNumber()));
        return saved;
    }

    public ActivitySchemaVersion createNewVersion(String existingSchemaVersionId) {
        ActivitySchemaVersion existing = schemaVersionRepository.findById(existingSchemaVersionId)
                .orElseThrow(() -> new EntityNotFoundException("ActivitySchemaVersion", existingSchemaVersionId));

        ActivitySchemaVersion newVersion = new ActivitySchemaVersion();
        newVersion.setTemplateId(existing.getTemplateId());
        newVersion.setVersionNumber(existing.getVersionNumber() + 1);
        newVersion.setStatus(SchemaStatus.DRAFT);
        newVersion.setEffectiveDate(LocalDate.now());
        ActivitySchemaVersion saved = schemaVersionRepository.save(newVersion);

        // Copy fields to new version
        List<ActivityFieldDefinition> fields = fieldDefinitionRepository
                .findBySchemaVersionIdAndActiveTrueOrderByDisplayOrder(existingSchemaVersionId);
        for (ActivityFieldDefinition f : fields) {
            ActivityFieldDefinition copy = new ActivityFieldDefinition();
            copy.setSchemaVersionId(saved.getId());
            copy.setFieldCode(f.getFieldCode());
            copy.setFieldLabel(f.getFieldLabel());
            copy.setFieldType(f.getFieldType());
            copy.setRequired(f.isRequired());
            copy.setDisplayOrder(f.getDisplayOrder());
            copy.setValidationRulesJson(f.getValidationRulesJson());
            copy.setHelpText(f.getHelpText());
            copy.setActive(true);
            fieldDefinitionRepository.save(copy);
        }
        return saved;
    }

    public ActivitySchemaVersion deprecateSchema(String schemaVersionId) {
        ActivitySchemaVersion version = schemaVersionRepository.findById(schemaVersionId)
                .orElseThrow(() -> new EntityNotFoundException("ActivitySchemaVersion", schemaVersionId));
        version.setStatus(SchemaStatus.DEPRECATED);
        return schemaVersionRepository.save(version);
    }

    @Transactional(readOnly = true)
    public List<ActivityTemplateDto> listBySport(String sportCode) {
        return templateRepository.findBySportCodeAndActiveTrue(sportCode).stream()
                .map(this::toTemplateDto).toList();
    }

    // ---- Mappers ---------------------------------------------------------

    private ActivityTemplateDto toTemplateDto(ActivityTemplate t) {
        return new ActivityTemplateDto(t.getId(), t.getCode(), t.getName(), t.getDescription(),
                null, t.getSportCode(), t.isActive());
    }

    private FieldDefinitionDto toFieldDto(ActivityFieldDefinition f) {
        return new FieldDefinitionDto(f.getId(), f.getFieldCode(), f.getFieldLabel(),
                f.getFieldType().name(), f.isRequired(), f.getDisplayOrder(),
                f.getValidationRulesJson(), f.getHelpText());
    }
}
