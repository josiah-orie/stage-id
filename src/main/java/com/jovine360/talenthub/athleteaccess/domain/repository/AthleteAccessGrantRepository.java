package com.jovine360.talenthub.athleteaccess.domain.repository;

import com.jovine360.talenthub.athleteaccess.domain.model.AthleteAccessGrant;
import com.jovine360.talenthub.athleteaccess.domain.model.AthletePermission;
import com.jovine360.talenthub.athleteaccess.domain.model.GranteeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AthleteAccessGrantRepository extends JpaRepository<AthleteAccessGrant, String> {
    List<AthleteAccessGrant> findByAthleteUserIdAndGranteeIdAndGranteeTypeAndActiveTrue(
            String athleteUserId, String granteeId, GranteeType granteeType);

    boolean existsByAthleteUserIdAndGranteeIdAndPermissionAndActiveTrue(
            String athleteUserId, String granteeId, AthletePermission permission);

    List<AthleteAccessGrant> findByAthleteUserIdAndActiveTrue(String athleteUserId);
}
