package com.jovine360.talenthub.guardian.api;

/**
 * Public interface of the guardian module, used by other modules for access decisions.
 */
public interface GuardianApi {

    /**
     * True if {@code guardianUserId} holds an ACTIVE, consented, unexpired PARENT or
     * LEGAL_GUARDIAN link to the athlete. AGENT links do not count: agents get explicit
     * access grants instead.
     */
    boolean hasActiveGuardianLink(String guardianUserId, String athleteUserId);

    /**
     * For a minor athlete (under 18), asserts that an ACTIVE consented guardian link exists.
     * Does nothing for adults.
     *
     * @throws com.jovine360.talenthub.shared.domain.exception.SafeguardingViolationException
     *         when the athlete is a minor with no guardian link
     */
    void assertGuardianConsentForMinor(String athleteUserId);
}
