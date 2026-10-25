package com.jovine360.talenthub;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the Spring Modulith module structure for all phases.
 *
 * <p>Phase 1 modules: shared, identity, profile, sport, subscription, athlete,
 * guardian, organization, athleteaccess.
 */
class ModularityTests {

    static final ApplicationModules modules =
            ApplicationModules.of(TalenthubApplication.class);

    @Test
    void modulesAreCompliant() {
        modules.verify();
    }

    @Test
    void generateModuleDocumentation() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

    // ---- Phase 1 acceptance tests ----------------------------------------

    @Test
    void phase1_sharedModuleExists() {
        var sharedModule = modules.getModuleByName("shared")
                .orElseThrow(() -> new AssertionError("shared module not found"));
        assertThat(sharedModule.getIdentifier().toString()).isEqualTo("shared");
    }

    @Test
    void phase1_identityModuleExists() {
        var module = modules.getModuleByName("identity")
                .orElseThrow(() -> new AssertionError("identity module not found"));
        assertThat(module.getIdentifier().toString()).isEqualTo("identity");
    }

    @Test
    void phase1_athleteModuleExists() {
        var module = modules.getModuleByName("athlete")
                .orElseThrow(() -> new AssertionError("athlete module not found"));
        assertThat(module.getIdentifier().toString()).isEqualTo("athlete");
    }

    @Test
    void phase1_allCoreModulesPresent() {
        var moduleNames = modules.stream()
                .map(m -> m.getIdentifier().toString())
                .toList();
        assertThat(moduleNames)
                .contains("shared", "identity", "profile", "sport", "subscription",
                        "athlete", "guardian", "organization", "athleteaccess");
    }
}
