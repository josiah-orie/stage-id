package com.jovine360.talenthub.sport.application.service;

import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.sport.api.SportConfigApi;
import com.jovine360.talenthub.sport.api.SportDto;
import com.jovine360.talenthub.sport.domain.model.Sport;
import com.jovine360.talenthub.sport.domain.repository.SportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SportService implements SportConfigApi {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    @Override
    public List<SportDto> listActiveSports() {
        return sportRepository.findByActiveTrue().stream().map(this::toDto).toList();
    }

    @Override
    public Optional<SportDto> findByCode(String code) {
        return sportRepository.findByCode(code).map(this::toDto);
    }

    @Override
    public boolean sportExists(String code) {
        return sportRepository.existsByCode(code);
    }

    @Override
    public Optional<SportDto> getSportById(String id) {
        return sportRepository.findById(id).map(this::toDto);
    }

    public SportDto getById(String id) {
        return sportRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Sport", id));
    }

    private SportDto toDto(Sport s) {
        return new SportDto(s.getId(), s.getCode(), s.getName(), s.getDescription(), s.isActive());
    }
}
