package com.jovine360.talenthub.scout.application.service;

import com.jovine360.talenthub.scout.api.SpecializationDto;
import com.jovine360.talenthub.scout.domain.model.ScoutSpecialization;
import com.jovine360.talenthub.scout.domain.model.ScoutingLevel;
import com.jovine360.talenthub.scout.domain.repository.ScoutSpecializationRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.sport.api.SportConfigApi;
import com.jovine360.talenthub.sport.api.SportDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/** The sports (and levels and regions) a scout focuses on. One entry per sport. */
@Service
@Transactional
public class ScoutSpecializationService {

    private final ScoutSpecializationRepository repository;
    private final SportConfigApi sportConfigApi;
    private final JsonMapper jsonMapper;

    public ScoutSpecializationService(ScoutSpecializationRepository repository,
                                      SportConfigApi sportConfigApi, JsonMapper jsonMapper) {
        this.repository = repository;
        this.sportConfigApi = sportConfigApi;
        this.jsonMapper = jsonMapper;
    }

    public SpecializationDto add(String scoutProfileId, String sportCode, ScoutingLevel level, List<String> regions) {
        SportDto sport = sportConfigApi.findByCode(sportCode)
                .orElseThrow(() -> new EntityNotFoundException("Sport", sportCode));
        if (repository.existsByScoutProfileIdAndSportId(scoutProfileId, sport.id())) {
            throw new DuplicateResourceException("You already have a specialization for " + sportCode);
        }
        ScoutSpecialization s = new ScoutSpecialization();
        s.setScoutProfileId(scoutProfileId);
        s.setSportId(sport.id());
        s.setLevel(level);
        s.setPreferredRegionsJson(regions == null || regions.isEmpty() ? null : jsonMapper.writeValueAsString(regions));
        return toDto(repository.save(s), sport.code());
    }

    @Transactional(readOnly = true)
    public List<SpecializationDto> list(String scoutProfileId) {
        return repository.findByScoutProfileId(scoutProfileId).stream().map(s -> toDto(s, null)).toList();
    }

    public void remove(String scoutProfileId, String specializationId) {
        ScoutSpecialization s = repository.findById(specializationId)
                .filter(x -> x.getScoutProfileId().equals(scoutProfileId))
                .orElseThrow(() -> new EntityNotFoundException("ScoutSpecialization", specializationId));
        repository.delete(s);
    }

    private SpecializationDto toDto(ScoutSpecialization s, String sportCode) {
        String code = sportCode;
        if (code == null) {
            try {
                code = sportConfigApi.getSportById(s.getSportId()).map(SportDto::code).orElse(null);
            } catch (RuntimeException e) {
                code = null;
            }
        }
        List<String> regions = List.of();
        if (s.getPreferredRegionsJson() != null) {
            try {
                regions = jsonMapper.readValue(s.getPreferredRegionsJson(), new TypeReference<List<String>>() {});
            } catch (RuntimeException ignored) {
                // stored value is not a JSON array; leave empty
            }
        }
        return new SpecializationDto(s.getId(), s.getScoutProfileId(), s.getSportId(), code,
                s.getLevel() != null ? s.getLevel().name() : null, regions);
    }
}
