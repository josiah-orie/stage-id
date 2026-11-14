package com.jovine360.talenthub.activity.api;

import java.util.Optional;

/** Operations other modules may perform on athlete activities. */
public interface ActivityApi {

    /** The athlete (user id) who owns the activity, if it exists. */
    Optional<String> findAthleteId(String activityId);

    /** Marks the activity VERIFIED. */
    void markVerified(String activityId);
}
