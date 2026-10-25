package com.jovine360.talenthub.organization.domain.service;

import com.jovine360.talenthub.organization.domain.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

/**
 * Generates a unique, human-readable organisation code from the org name.
 * Example: "FC Barcelona" → "FCB"; "Manchester United" → "MU".
 */
@Service
public class OrgCodeGeneratorService {

    private final OrganizationRepository orgRepository;

    public OrgCodeGeneratorService(OrganizationRepository orgRepository) {
        this.orgRepository = orgRepository;
    }

    public String generate(String orgName) {
        String base = deriveBase(orgName);
        if (!orgRepository.existsByCode(base)) return base;
        // Append numeric suffix until unique
        for (int i = 2; i <= 999; i++) {
            String candidate = base + i;
            if (!orgRepository.existsByCode(candidate)) return candidate;
        }
        throw new IllegalStateException("Could not generate unique org code for: " + orgName);
    }

    private String deriveBase(String name) {
        String[] words = name.trim().toUpperCase().replaceAll("[^A-Z0-9 ]", "").split("\\s+");
        if (words.length >= 3) {
            return String.valueOf(words[0].charAt(0))
                    + words[1].charAt(0)
                    + words[2].charAt(0);
        } else if (words.length == 2) {
            return String.valueOf(words[0].charAt(0)) + words[1].charAt(0);
        } else {
            return words[0].substring(0, Math.min(3, words[0].length()));
        }
    }
}
