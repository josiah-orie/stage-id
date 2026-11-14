package com.jovine360.talenthub.scouting.domain.repository;

import com.jovine360.talenthub.scouting.domain.model.AthleteFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AthleteFollowRepository extends JpaRepository<AthleteFollow, String> {
    List<AthleteFollow> findByAthleteIdAndNotificationsEnabledTrue(String athleteId);
    List<AthleteFollow> findByFollowerId(String followerId);
    java.util.Optional<AthleteFollow> findFirstByFollowerIdAndAthleteId(String followerId, String athleteId);
    List<AthleteFollow> findByFollowerIdAndAthleteId(String followerId, String athleteId);
}
