package com.jovine360.talenthub.activityconfiguration.infrastructure.web;

import com.jovine360.talenthub.activityconfiguration.api.FieldDefinitionDto;
import com.jovine360.talenthub.activityconfiguration.application.service.ActivityConfigurationService;
import com.jovine360.talenthub.activityconfiguration.domain.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminActivityConfigController {

    private final ActivityConfigurationService configService;

    public AdminActivityConfigController(ActivityConfigurationService configService) {
        this.configService = configService;
    }

    record CreateActivityTypeRequest(@NotBlank String name, String description) {}
    record CreateTemplateRequest(@NotBlank String activityTypeId, String sportId,
                                  @NotBlank String name, String description) {}
    record AddFieldRequest(@NotBlank String fieldCode, @NotBlank String fieldLabel,
                            @NotNull FieldType fieldType, boolean required, int displayOrder) {}

    @PostMapping("/activity-types")
    public ResponseEntity<ActivityType> createActivityType(@Valid @RequestBody CreateActivityTypeRequest req) {
        ActivityType type = new ActivityType();
        type.setName(req.name());
        type.setDescription(req.description());
        type.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(type);
    }

    @PostMapping("/activity-templates")
    public ResponseEntity<ActivityTemplate> createTemplate(@Valid @RequestBody CreateTemplateRequest req) {
        ActivityTemplate template = configService.createTemplate(
                req.activityTypeId(), req.sportId(), req.name(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    @PostMapping("/activity-templates/{id}/fields")
    public ResponseEntity<FieldDefinitionDto> addField(@PathVariable String id,
                                                        @Valid @RequestBody AddFieldRequest req) {
        ActivityFieldDefinition field = configService.addField(
                id, req.fieldCode(), req.fieldLabel(), req.fieldType(), req.required(), req.displayOrder());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FieldDefinitionDto(field.getId(), field.getFieldCode(), field.getFieldLabel(),
                        field.getFieldType().name(), field.isRequired(), field.getDisplayOrder(),
                        field.getValidationRulesJson(), field.getHelpText()));
    }

    @PostMapping("/activity-schema-versions/{id}/publish")
    public ResponseEntity<ActivitySchemaVersion> publishSchema(@PathVariable String id) {
        // publishedBy from security context would be resolved here in real impl
        ActivitySchemaVersion published = configService.publishSchema(id, "system");
        return ResponseEntity.ok(published);
    }
}
