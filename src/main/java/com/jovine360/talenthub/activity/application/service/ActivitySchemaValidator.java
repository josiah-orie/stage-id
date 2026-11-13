package com.jovine360.talenthub.activity.application.service;

import com.jovine360.talenthub.activityconfiguration.api.ActivityConfigApi;
import com.jovine360.talenthub.activityconfiguration.api.FieldDefinitionDto;
import com.jovine360.talenthub.activityconfiguration.api.PublishedSchemaDto;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Validates field values submitted for an athlete activity against the published schema.
 */
@Component
public class ActivitySchemaValidator {

    private final ActivityConfigApi activityConfigApi;

    public ActivitySchemaValidator(ActivityConfigApi activityConfigApi) {
        this.activityConfigApi = activityConfigApi;
    }

    /**
     * Validates that all required fields are present and values match the field type.
     *
     * @param templateId the activity template ID
     * @param fieldValues map of fieldCode → raw string value
     * @throws IllegalArgumentException if validation fails
     */
    public PublishedSchemaDto validateAndGetSchema(String templateId, Map<String, String> fieldValues) {
        PublishedSchemaDto schema = activityConfigApi.getPublishedSchema(templateId)
                .orElseThrow(() -> new EntityNotFoundException("PublishedSchema", templateId));

        List<FieldDefinitionDto> fields = schema.fields();
        for (FieldDefinitionDto field : fields) {
            String value = fieldValues.get(field.fieldCode());
            if (field.required() && (value == null || value.isBlank())) {
                throw new IllegalArgumentException(
                        "Required field missing: " + field.fieldCode());
            }
            if (value != null && !value.isBlank()) {
                validateType(field.fieldCode(), field.fieldType(), value);
            }
        }
        return schema;
    }

    private void validateType(String fieldCode, String fieldType, String value) {
        try {
            switch (fieldType) {
                case "INTEGER" -> Integer.parseInt(value.trim());
                case "DECIMAL" -> new BigDecimal(value.trim());
                case "BOOLEAN" -> {
                    String v = value.trim().toLowerCase();
                    if (!v.equals("true") && !v.equals("false") && !v.equals("1") && !v.equals("0")) {
                        throw new IllegalArgumentException("Invalid boolean value for field: " + fieldCode);
                    }
                }
                // TEXT, LONG_TEXT, DATE, DATETIME, SINGLE_SELECT, MULTI_SELECT: accept any non-blank string
                default -> { /* accepted */ }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldType + " value for field '" + fieldCode + "': " + value);
        }
    }
}
