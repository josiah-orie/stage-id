package com.jovine360.talenthub;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the Spring Modulith module structure.
 * Each phase's integration task will expand these assertions.
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
}
