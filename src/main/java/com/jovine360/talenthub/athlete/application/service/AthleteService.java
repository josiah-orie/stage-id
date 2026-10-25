package com.jovine360.talenthub.athlete.application.service;

import com.jovine360.talenthub.athlete.api.AthleteApi;
import com.jovine360.talenthub.athlete.api.AthleteProfileDto;
import com.jovine360.talenthub.athlete.api.CreateAthleteProfileRequest;
import com.jovine360.talenthub.athlete.domain.model.AthleteProfile;
import com.jovine360.talenthub.athlete.domain.repository.AthleteProfileRepository;
import com.jovine360.talenthub.athlete.domain.service.AthleteNumberGeneratorService;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.sport.api.SportConfigApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AthleteService implements AthleteApi {

    private final AthleteProfileRepository athleteProfileRepository;
    private final AthleteNumberGeneratorService numberGenerator;
    private final SportConfigApi sportConfigApi;

    public AthleteService(AthleteProfileRepository athleteProfileRepository,
                          AthleteNumberGeneratorService numberGenerator,
                          SportConfigApi sportConfigApi) {
        this.athleteProfileRepository = athleteProfileRepository;
        this.numberGenerator = numberGenerator;
        this.sportConfigApi = sportConfigApi;
    }

    public AthleteProfileDto createAthleteProfile(String userId, CreateAthleteProfileRequest req) {
        if (athleteProfileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("AthleteProfile", "userId", userId);
        }

        AthleteProfile profile = new AthleteProfile();
        profile.setUserId(userId);
        profile.setPosition(req.position());
        profile.setHeightCm(req.heightCm());
        profile.setWeightKg(req.weightKg());
        profile.setDominantFoot(req.dominantFoot());
        profile.setNationality(req.nationality());

        if (req.primarySportCode() != null) {
            sportConfigApi.findByCode(req.primarySportCode()).ifPresent(sport -> {
                profile.setPrimarySportId(sport.id());
                profile.setPrimarySportCode(sport.code());
            });
        }

        // Generate athlete number (unaffiliated for now — org affiliation sets a new number)
        String athleteNumber = numberGenerator.generateUnaffiliatedNumber();
        profile.setAthleteNumber(athleteNumber);

        return toDto(athleteProfileRepository.save(profile));
    }

    public AthleteProfileDto updateAthleteProfile(String userId, CreateAthleteProfileRequest req) {
        AthleteProfile profile = athleteProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("AthleteProfile", userId));

        if (req.primarySportCode() != null) {
            sportConfigApi.findByCode(req.primarySportCode()).ifPresent(sport -> {
                profile.setPrimarySportId(sport.id());
                profile.setPrimarySportCode(sport.code());
            });
        }
        if (req.position()     != null) profile.setPosition(req.position());
        if (req.heightCm()     != null) profile.setHeightCm(req.heightCm());
        if (req.weightKg()     != null) profile.setWeightKg(req.weightKg());
        if (req.dominantFoot() != null) profile.setDominantFoot(req.dominantFoot());
        if (req.nationality()  != null) profile.setNationality(req.nationality());

        return toDto(athleteProfileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AthleteProfileDto> findByUserId(String userId) {
        return athleteProfileRepository.findByUserId(userId).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AthleteProfileDto> findByAthleteNumber(String athleteNumber) {
        return athleteProfileRepository.findByAthleteNumber(athleteNumber).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAthleteUser(String userId) {
        return athleteProfileRepository.existsByUserId(userId);
    }

    public AthleteProfileDto getByUserId(String userId) {
        return athleteProfileRepository.findByUserId(userId)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("AthleteProfile", userId));
    }

    private AthleteProfileDto toDto(AthleteProfile p) {
        return new AthleteProfileDto(
                p.getId(), p.getUserId(), p.getAthleteNumber(),
                p.getPrimarySportId(), p.getPrimarySportCode(),
                p.getPosition(), p.getHeightCm(), p.getWeightKg(),
                p.getDominantFoot(), p.getStatus() != null ? p.getStatus().name() : null,
                p.getOrganizationId(), p.getNationality()
        );
    }
}
