package com.jovine360.talenthub.sport.application.service;

import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.sport.api.SportAdminDtos.*;
import com.jovine360.talenthub.sport.api.SportDto;
import com.jovine360.talenthub.sport.domain.model.Sport;
import com.jovine360.talenthub.sport.domain.model.SportDiscipline;
import com.jovine360.talenthub.sport.domain.model.SportMetricDefinition;
import com.jovine360.talenthub.sport.domain.repository.SportDisciplineRepository;
import com.jovine360.talenthub.sport.domain.repository.SportMetricDefinitionRepository;
import com.jovine360.talenthub.sport.domain.repository.SportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SportAdministrationService {

    private final SportRepository sportRepository;
    private final SportDisciplineRepository disciplineRepository;
    private final SportMetricDefinitionRepository metricRepository;

    public SportAdministrationService(SportRepository sportRepository,
                                      SportDisciplineRepository disciplineRepository,
                                      SportMetricDefinitionRepository metricRepository) {
        this.sportRepository = sportRepository;
        this.disciplineRepository = disciplineRepository;
        this.metricRepository = metricRepository;
    }

    public SportDto createSport(CreateSportRequest req) {
        if (sportRepository.existsByCode(req.code())) {
            throw new DuplicateResourceException("Sport", "code", req.code());
        }
        Sport s = new Sport();
        s.setCode(req.code());
        s.setName(req.name());
        s.setDescription(req.description());
        s.setActive(true);
        Sport saved = sportRepository.save(s);
        return new SportDto(saved.getId(), saved.getCode(), saved.getName(), saved.getDescription(), saved.isActive());
    }

    public DisciplineDto addDiscipline(String sportId, CreateDisciplineRequest req) {
        assertSport(sportId);
        if (disciplineRepository.existsBySportIdAndCode(sportId, req.code())) {
            throw new DuplicateResourceException("SportDiscipline", "code", req.code());
        }
        SportDiscipline d = new SportDiscipline();
        d.setSportId(sportId);
        d.setCode(req.code());
        d.setName(req.name());
        return toDto(disciplineRepository.save(d));
    }

    public MetricDto addMetric(String sportId, CreateMetricRequest req) {
        assertSport(sportId);
        if (metricRepository.existsBySportIdAndCode(sportId, req.code())) {
            throw new DuplicateResourceException("SportMetricDefinition", "code", req.code());
        }
        SportMetricDefinition m = new SportMetricDefinition();
        m.setSportId(sportId);
        m.setCode(req.code());
        m.setName(req.name());
        m.setUnit(req.unit());
        m.setHigherIsBetter(req.higherIsBetter() == null || req.higherIsBetter());
        return toDto(metricRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<DisciplineDto> listDisciplines(String sportId) {
        assertSport(sportId);
        return disciplineRepository.findBySportIdOrderByName(sportId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<MetricDto> listMetrics(String sportId) {
        assertSport(sportId);
        return metricRepository.findBySportIdOrderByName(sportId).stream().map(this::toDto).toList();
    }

    private void assertSport(String sportId) {
        if (!sportRepository.existsById(sportId)) {
            throw new EntityNotFoundException("Sport", sportId);
        }
    }

    private DisciplineDto toDto(SportDiscipline d) {
        return new DisciplineDto(d.getId(), d.getSportId(), d.getCode(), d.getName(), d.isActive());
    }

    private MetricDto toDto(SportMetricDefinition m) {
        return new MetricDto(m.getId(), m.getSportId(), m.getCode(), m.getName(), m.getUnit(),
                m.isHigherIsBetter(), m.isActive());
    }
}
