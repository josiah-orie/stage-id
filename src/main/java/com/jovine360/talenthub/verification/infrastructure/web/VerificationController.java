package com.jovine360.talenthub.verification.infrastructure.web;

import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import com.jovine360.talenthub.verification.application.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','COACH')")
    public ResponseEntity<Void> verifyActivity(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        verificationService.verifyActivity(id, principal.userId(), principal.roles());
        return ResponseEntity.ok().build();
    }
}
