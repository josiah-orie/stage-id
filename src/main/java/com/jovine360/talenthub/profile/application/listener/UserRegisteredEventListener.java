package com.jovine360.talenthub.profile.application.listener;

import com.jovine360.talenthub.identity.domain.event.UserRegisteredEvent;
import com.jovine360.talenthub.profile.application.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens to {@link UserRegisteredEvent} published by the identity module
 * and creates an initial profile for the new user.
 */
@Component
public class UserRegisteredEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    private final ProfileService profileService;

    public UserRegisteredEventListener(ProfileService profileService) {
        this.profileService = profileService;
    }

    @ApplicationModuleListener
    public void on(UserRegisteredEvent event) {
        log.info("Creating profile for new user: {}", event.userId());
        profileService.createProfile(event.userId(), event.email(), event.role());
    }
}
