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

    private final com.jovine360.talenthub.shared.domain.service.AuditTrail audit;

    public AdminActivityConfigController(ActivityConfigurationService configService,
                                         com.jovine360.talenthub.shared.domain.service.AuditTrail audit) {
        this.configService = configService;
        this.audit = audit;
    }

    record CreateActivityTypeRequest(@NotNull ActivityTypeCode code, @NotBlank String name, String description) {}
    record CreateTemplateRequest(@NotBlank String activityTypeId, String sportId,
                                  @NotBlank String name, String description) {}
    record AddFieldRequest(@NotBlank String fieldCode, @NotBlank String fieldLabel,
                            @NotNull FieldType fieldType, boolean required, int displayOrder) {}

    @PostMapping("/activity-types")
    public ResponseEntity<ActivityType> createActivityType(@Valid @RequestBody CreateActivityTypeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(configService.createActivityType(req.code(), req.name(), req.description()));
    }

    @PostMapping("/activity-templates")
    public ResponseEntity<ActivityTemplate> createTemplate(@Valid @RequestBody CreateTemplateRequest req) {
        ActivityTemplate template = configService.createTemplate(
                req.activityTypeId(), req.sportId(), req.name(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    /** Adds a field to the template's current DRAFT schema version (created if needed). */
    @PostMapping("/activity-templates/{id}/fields")
    public ResponseEntity<FieldDefinitionDto> addField(@PathVariable String id,
                                                        @Valid @RequestBody AddFieldRequest req) {
        ActivitySchemaVersion draft = configService.currentDraft(id);
        ActivityFieldDefinition field = configService.addField(
                draft.getId(), req.fieldCode(), req.fieldLabel(), req.fieldType(), req.required(), req.displayOrder());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FieldDefinitionDto(field.getId(), field.getFieldCode(), field.getFieldLabel(),
                        field.getFieldType().name(), field.isRequired(), field.getDisplayOrder(),
                        field.getValidationRulesJson(), field.getHelpText()));
    }

    /** Every schema version of a template, newest first. Use the DRAFT one's id to publish. */
    @GetMapping("/activity-templates/{id}/schema-versions")
    public ResponseEntity<java.util.List<ActivitySchemaVersion>> versions(@PathVariable String id) {
        return ResponseEntity.ok(configService.listVersions(id));
    }

    /** Retires a PUBLISHED version. Reserved for SUPER_ADMIN, like publishing. */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/activity-schema-versions/{id}/deprecate")
    public ResponseEntity<ActivitySchemaVersion> deprecateSchema(@PathVariable String id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal principal) {
        ActivitySchemaVersion result = configService.deprecateSchema(id);
        audit.record(principal.userId(), principal.roles(), "SCHEMA_DEPRECATED", "ActivitySchemaVersion", id, null);
        return ResponseEntity.ok(result);
    }

    /** Publishing changes what every athlete can submit, so it is reserved for SUPER_ADMIN. */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/activity-schema-versions/{id}/publish")
    public ResponseEntity<ActivitySchemaVersion> publishSchema(@PathVariable String id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal principal) {
        ActivitySchemaVersion published = configService.publishSchema(id, principal.userId());
        audit.record(principal.userId(), principal.roles(), "SCHEMA_PUBLISHED", "ActivitySchemaVersion", id, null);
        return ResponseEntity.ok(published);
    }
}
