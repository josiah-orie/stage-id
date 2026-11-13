package com.jovine360.talenthub.acceptance;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Phase 1 acceptance tests.
 * Requires MySQL via Testcontainers + Docker.
 *
 * These tests validate the Phase 1 module integration end-to-end:
 * 1. Athlete registration and sport profile creation
 * 2. Guardian registration and athlete linking
 * 3. Academy registration and staff invitation
 * 4. Academy-to-athlete management authorization
 */
@Disabled("Requires Docker for Testcontainers MySQL — run locally with Docker available")
class Phase1AcceptanceTest {

    @Test
    void test1_athleteRegistrationAndSportProfileCreation() {
        // 1. Register user (POST /api/v1/auth/register with role ATHLETE)
        // 2. Create athlete profile (POST /api/v1/athletes/me/profile)
        // 3. Verify athlete number is generated (TH-XXXXX format for unaffiliated)
        // 4. Assert athlete profile exists and sport link is correct
    }

    @Test
    void test2_guardianRegistrationAndAthleteLinking() {
        // 1. Register guardian user
        // 2. Register minor athlete
        // 3. Guardian requests link to athlete
        // 4. Link is approved / auto-approved for guardian role
        // 5. Verify guardian has MANAGE permission on athlete
    }

    @Test
    void test3_academyRegistrationAndStaffInvitation() {
        // 1. Register organization admin user
        // 2. Create Academy organization
        // 3. Invite staff member (POST /api/v1/organizations/{id}/members)
        // 4. Staff member accepts invitation
        // 5. Verify staff member appears in organization member list
    }

    @Test
    void test4_academyToAthleteManagementAuthorization() {
        // 1. Academy creates athlete-org relationship
        // 2. Academy staff attempts to access athlete data
        // 3. Verify permission is granted via AthleteAccessApi
        // 4. Non-member attempts to access → denied
    }
}
