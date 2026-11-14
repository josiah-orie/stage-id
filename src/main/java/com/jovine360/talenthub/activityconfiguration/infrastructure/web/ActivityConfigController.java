package com.jovine360.talenthub.activityconfiguration.infrastructure.web;

import com.jovine360.talenthub.activityconfiguration.api.ActivityTemplateDto;
import com.jovine360.talenthub.activityconfiguration.api.PublishedSchemaDto;
import com.jovine360.talenthub.activityconfiguration.application.service.ActivityConfigurationService;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ActivityConfigController {

    private final ActivityConfigurationService configService;

    public ActivityConfigController(ActivityConfigurationService configService) {
        this.configService = configService;
    }

    @GetMapping("/activity-types/{id}/schema")
    public ResponseEntity<PublishedSchemaDto> getSchema(@PathVariable String id) {
        return configService.getPublishedSchema(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("ActivitySchemaVersion", id));
    }

    @GetMapping("/sports/{id}/activity-types")
    public ResponseEntity<List<ActivityTemplateDto>> getActivityTypesBySport(@PathVariable String id) {
        // {id} may be the sport's id or its code
        return ResponseEntity.ok(configService.listBySportIdOrCode(id));
    }
}
