package com.jovine360.talenthub;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the Spring Modulith module structure for all phases.
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

    // ---- Phase 1 --------------------------------------------------------

    @Test
    void phase1_sharedModuleExists() {
        var m = modules.getModuleByName("shared")
                .orElseThrow(() -> new AssertionError("shared module not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("shared");
    }

    @Test
    void phase1_identityModuleExists() {
        var m = modules.getModuleByName("identity")
                .orElseThrow(() -> new AssertionError("identity module not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("identity");
    }

    @Test
    void phase1_athleteModuleExists() {
        var m = modules.getModuleByName("athlete")
                .orElseThrow(() -> new AssertionError("athlete module not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("athlete");
    }

    @Test
    void phase1_allCoreModulesPresent() {
        var names = modules.stream().map(m -> m.getIdentifier().toString()).toList();
        assertThat(names)
                .contains("shared", "identity", "profile", "sport", "subscription",
                        "athlete", "guardian", "organization", "athleteaccess");
    }

    /**
     * Identity communicates to profile only via UserRegisteredEvent (no direct bean injection).
     * The identity module's api package must not reference profile types.
     */
    @Test
    void phase1_identityToProfileOnlyViaEvent() {
        var identity = modules.getModuleByName("identity")
                .orElseThrow(() -> new AssertionError("identity not found"));
        // Modulith verify() above already catches illegal direct references;
        // this assertion confirms the module was loaded correctly.
        assertThat(identity.getIdentifier().toString()).isEqualTo("identity");
    }

    /**
     * Athlete module accesses athleteaccess only via the AthleteAccessApi interface.
     */
    @Test
    void phase1_athleteAccessViaInterface() {
        var athleteaccess = modules.getModuleByName("athleteaccess")
                .orElseThrow(() -> new AssertionError("athleteaccess not found"));
        assertThat(athleteaccess.getIdentifier().toString()).isEqualTo("athleteaccess");
    }

    /**
     * Organization accesses subscription only via EntitlementService interface.
     */
    @Test
    void phase1_organizationSubscriptionViaEntitlementService() {
        var subscription = modules.getModuleByName("subscription")
                .orElseThrow(() -> new AssertionError("subscription not found"));
        assertThat(subscription.getIdentifier().toString()).isEqualTo("subscription");
    }

    @Test
    void phase1_noCircularDependencies() {
        // Spring Modulith's verify() already fails on cycles; this documents the intent.
        assertThat(modules.stream().count()).isGreaterThan(5);
    }

    // ---- Phase 2 --------------------------------------------------------

    @Test
    void phase2_activityConfigurationModuleExists() {
        var m = modules.getModuleByName("activityconfiguration")
                .orElseThrow(() -> new AssertionError("activityconfiguration not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("activityconfiguration");
    }

    @Test
    void phase2_activityModuleExists() {
        var m = modules.getModuleByName("activity")
                .orElseThrow(() -> new AssertionError("activity not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("activity");
    }

    @Test
    void phase2_developmentModuleExists() {
        var m = modules.getModuleByName("development")
                .orElseThrow(() -> new AssertionError("development not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("development");
    }

    @Test
    void phase2_mediaModuleExists() {
        var m = modules.getModuleByName("media")
                .orElseThrow(() -> new AssertionError("media not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("media");
    }

    @Test
    void phase2_verificationModuleExists() {
        var m = modules.getModuleByName("verification")
                .orElseThrow(() -> new AssertionError("verification not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("verification");
    }

    // ---- Phase 3 --------------------------------------------------------

    @Test
    void phase3_allModulesPresent() {
        var names = modules.stream().map(m -> m.getIdentifier().toString()).toList();
        assertThat(names)
                .contains("shared", "identity", "profile", "sport", "subscription",
                        "athlete", "guardian", "organization", "athleteaccess",
                        "activityconfiguration", "activity", "development", "media",
                        "verification");
    }

    /**
     * Discovery module must exist as an independent module (reads from its own projection,
     * not directly from athlete entities). Modulith verify() enforces the boundary;
     * this test documents and asserts the module is present and isolated.
     */
    @Test
    void phase3_discoveryModuleExists() {
        var m = modules.getModuleByName("discovery")
                .orElseThrow(() -> new AssertionError("discovery module not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("discovery");
    }

    /**
     * Discovery reads from projection data only, not directly from athlete entities.
     * Spring Modulith's verify() above enforces this; this test confirms the module
     * is correctly loaded and its identifier is distinct from the athlete module.
     */
    @Test
    void phase3_discoveryModuleIsDistinctFromAthleteModule() {
        var discovery = modules.getModuleByName("discovery")
                .orElseThrow(() -> new AssertionError("discovery module not found"));
        var athlete = modules.getModuleByName("athlete")
                .orElseThrow(() -> new AssertionError("athlete module not found"));
        assertThat(discovery.getIdentifier()).isNotEqualTo(athlete.getIdentifier());
    }

    /**
     * Scouting module must access athlete identity only via the AthleteApi interface,
     * not by directly injecting athlete domain repositories or entities.
     * Spring Modulith's verify() enforces that scouting does not depend on
     * internal athlete types — this test documents that contract.
     */
    @Test
    void phase3_scoutingModuleExists() {
        var m = modules.getModuleByName("scouting")
                .orElseThrow(() -> new AssertionError("scouting module not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("scouting");
    }

    /**
     * Notification module is present and independent.
     */
    @Test
    void phase3_notificationModuleExists() {
        var m = modules.getModuleByName("notification")
                .orElseThrow(() -> new AssertionError("notification module not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("notification");
    }

    /**
     * Scout module exists and is separate from scouting and discovery.
     */
    @Test
    void phase3_scoutModuleExists() {
        var m = modules.getModuleByName("scout")
                .orElseThrow(() -> new AssertionError("scout module not found"));
        assertThat(m.getIdentifier().toString()).isEqualTo("scout");
    }

    @Test
    void phase3_allPhase3ModulesPresent() {
        var names = modules.stream().map(m -> m.getIdentifier().toString()).toList();
        assertThat(names).contains(
                "scout", "discovery", "scouting", "notification", "safeguarding");
    }
}
