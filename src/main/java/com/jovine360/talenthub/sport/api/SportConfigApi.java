package com.jovine360.talenthub.sport.api;

import java.util.List;
import java.util.Optional;

/**
 * Public interface for the sport configuration module.
 */
public interface SportConfigApi {
    List<SportDto> listActiveSports();
    Optional<SportDto> findByCode(String code);
    boolean sportExists(String code);
}
