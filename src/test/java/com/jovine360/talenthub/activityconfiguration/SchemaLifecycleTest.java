package com.jovine360.talenthub.activityconfiguration;

import com.jovine360.talenthub.activityconfiguration.application.service.ActivityConfigurationService;
import com.jovine360.talenthub.activityconfiguration.domain.model.*;
import com.jovine360.talenthub.activityconfiguration.domain.repository.*;
import com.jovine360.talenthub.shared.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Template → draft v1 → fields → publish → new draft. In-memory stubs, no Spring. */
class SchemaLifecycleTest {

    private final List<ActivitySchemaVersion> versions = new ArrayList<>();
    private ActivityConfigurationService service;

    @BeforeEach
    void setUp() {
        ActivityTemplateRepository templates = mock(ActivityTemplateRepository.class);
        when(templates.save(any())).thenAnswer(i -> { ActivityTemplate t = i.getArgument(0); t.setId("tmpl-1"); return t; });
        when(templates.existsById("tmpl-1")).thenReturn(true);

        ActivitySchemaVersionRepository repo = mock(ActivitySchemaVersionRepository.class);
        when(repo.save(any())).thenAnswer(i -> {
            ActivitySchemaVersion v = i.getArgument(0);
            if (v.getId() == null) v.setId("v" + (versions.size() + 1));
            if (!versions.contains(v)) versions.add(v);
            return v;
        });
        when(repo.findById(anyString())).thenAnswer(i -> versions.stream().filter(v -> v.getId().equals(i.getArgument(0))).findFirst());
        when(repo.findFirstByTemplateIdAndStatusOrderByVersionNumberDesc(anyString(), any())).thenAnswer(i -> versions.stream()
                .filter(v -> v.getTemplateId().equals(i.getArgument(0)) && v.getStatus() == i.getArgument(1))
                .max(Comparator.comparingInt(ActivitySchemaVersion::getVersionNumber)));
        when(repo.findByTemplateIdOrderByVersionNumberDesc(anyString())).thenAnswer(i -> versions.stream()
                .filter(v -> v.getTemplateId().equals(i.getArgument(0)))
                .sorted(Comparator.comparingInt(ActivitySchemaVersion::getVersionNumber).reversed()).toList());

        ActivityFieldDefinitionRepository fields = mock(ActivityFieldDefinitionRepository.class);
        when(fields.save(any())).thenAnswer(i -> i.getArgument(0));
        service = new ActivityConfigurationService(templates, mock(ActivityTypeRepository.class), repo, fields,
                mock(ActivityFieldOptionRepository.class), mock(ApplicationEventPublisher.class));
    }

    @Test
    void templateGetsDraftV1AndFieldsGoToTheDraft() {
        service.createTemplate("type-1", "sport-1", "Rugby 7s Match", null);
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0).getStatus()).isEqualTo(SchemaStatus.DRAFT);

        ActivitySchemaVersion draft = service.currentDraft("tmpl-1");
        assertThat(draft.getVersionNumber()).isEqualTo(1);
        assertThat(service.addField(draft.getId(), "tries", "Tries", FieldType.INTEGER, false, 1).getSchemaVersionId())
                .isEqualTo("v1");
    }

    @Test
    void afterPublishTheNextDraftIsVersion2() {
        service.createTemplate("type-1", "sport-1", "Rugby 7s Match", null);
        service.publishSchema("v1", "super-1");

        ActivitySchemaVersion next = service.currentDraft("tmpl-1");
        assertThat(next.getVersionNumber()).isEqualTo(2);
        assertThat(next.getStatus()).isEqualTo(SchemaStatus.DRAFT);

        assertThatThrownBy(() -> service.publishSchema("v1", "super-1")).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> service.deprecateSchema(next.getId())).isInstanceOf(InvalidStateTransitionException.class);
        assertThat(service.deprecateSchema("v1").getStatus()).isEqualTo(SchemaStatus.DEPRECATED);
    }
}
