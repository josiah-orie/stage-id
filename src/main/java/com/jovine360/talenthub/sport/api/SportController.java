package com.jovine360.talenthub.sport.api;

import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import com.jovine360.talenthub.sport.application.service.SportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sports")
@Tag(name = "Sports", description = "Sport configuration reference data")
public class SportController {

    private final SportService sportService;

    public SportController(SportService sportService) {
        this.sportService = sportService;
    }

    @Operation(summary = "List all active sports")
    @GetMapping
    public ResponseEntity<List<SportDto>> listSports() {
        return ResponseEntity.ok(sportService.listActiveSports());
    }

    @Operation(summary = "Get a sport by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SportDto> getSport(@PathVariable String id) {
        return ResponseEntity.ok(sportService.getById(id));
    }

    @Operation(summary = "Get a sport by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<SportDto> getSportByCode(@PathVariable String code) {
        return ResponseEntity.ok(
                sportService.findByCode(code)
                        .orElseThrow(() -> new EntityNotFoundException("Sport", code)));
    }
}
