package com.jovine360.talenthub.safeguarding.api;

/** Published when consent for a purpose is granted for an athlete. */
public record AthleteConsentGrantedEvent(String athleteId, String purposeCode, String grantedBy) {}
