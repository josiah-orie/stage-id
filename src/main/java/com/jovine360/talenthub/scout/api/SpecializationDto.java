package com.jovine360.talenthub.scout.api;

import java.util.List;

public record SpecializationDto(String id, String scoutProfileId, String sportId, String sportCode,
                                String level, List<String> preferredRegions) {}
