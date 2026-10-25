package com.jovine360.talenthub.profile.domain.model;

import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.shared.domain.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Base user profile — holds personal details common to all roles.
 * Created automatically when a user registers.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
public class UserProfile extends AuditableEntity {

    /** FK to identity.users — not a JPA relation (cross-module boundary). */
    @Column(name = "user_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String userId;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "country_code", columnDefinition = "CHAR(2)")
    private String countryCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Column(name = "profile_complete", nullable = false)
    private boolean profileComplete = false;

    /** Computes a simple completeness check. */
    public void refreshCompleteness() {
        this.profileComplete = firstName != null && !firstName.isBlank()
                && lastName != null && !lastName.isBlank()
                && dateOfBirth != null
                && countryCode != null;
    }
}
