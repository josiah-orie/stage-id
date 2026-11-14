package com.jovine360.talenthub.organization.api;

import com.jovine360.talenthub.organization.application.service.OrganizationService;
import com.jovine360.talenthub.shared.infrastructure.configuration.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Organizations", description = "Organizations, members, invitations and athlete affiliations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // ---- organizations ----

    @Operation(summary = "Register an organization. The caller becomes its OWNER and an ORG_ADMIN")
    @PostMapping("/organizations")
    public ResponseEntity<OrganizationDto> create(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(principal.userId(), request));
    }

    @Operation(summary = "Organizations the caller is a member of")
    @GetMapping("/organizations/mine")
    public ResponseEntity<List<OrganizationDto>> mine(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.getMyOrganizations(principal.userId()));
    }

    @Operation(summary = "Get an organization's public details")
    @GetMapping("/organizations/{id}")
    public ResponseEntity<OrganizationDto> get(@PathVariable String id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @Operation(summary = "Update an organization (OWNER or ADMIN member)")
    @PatchMapping("/organizations/{id}")
    public ResponseEntity<OrganizationDto> update(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.update(id, principal.userId(), principal.roles(), request));
    }

    // ---- members & invitations ----

    @Operation(summary = "List active members (members only)")
    @GetMapping("/organizations/{id}/members")
    public ResponseEntity<List<OrganizationMemberDto>> members(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.listMembers(id, principal.userId(), principal.roles()));
    }

    @Operation(summary = "Remove a member (OWNER or ADMIN), or leave the organization yourself")
    @DeleteMapping("/organizations/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String id, @PathVariable String userId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        organizationService.removeMember(id, userId, principal.userId(), principal.roles());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Invite someone by email (OWNER or ADMIN). Returns the invitation token")
    @PostMapping("/organizations/{id}/invitations")
    public ResponseEntity<InvitationDto> invite(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody InviteMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.invite(id, principal.userId(), principal.roles(), request));
    }

    @Operation(summary = "List pending invitations (OWNER or ADMIN)")
    @GetMapping("/organizations/{id}/invitations")
    public ResponseEntity<List<InvitationDto>> invitations(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.listPendingInvitations(id, principal.userId(), principal.roles()));
    }

    @Operation(summary = "Accept an invitation sent to the caller's email address")
    @PostMapping("/organizations/{id}/invitations/accept")
    public ResponseEntity<OrganizationMemberDto> accept(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AcceptInvitationRequest request) {
        return ResponseEntity.ok(organizationService.acceptInvitation(
                id, principal.userId(), principal.email(), request.invitationToken()));
    }

    // ---- athlete affiliations ----

    @Operation(summary = "Ask an athlete to affiliate with the organization (OWNER, ADMIN or ATHLETE_MANAGER)")
    @PostMapping("/organizations/{id}/athlete-relationships")
    public ResponseEntity<AthleteAffiliationDto> requestAffiliation(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RequestAffiliationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.requestAffiliation(id, principal.userId(), principal.roles(), request));
    }

    @Operation(summary = "List the organization's athlete affiliations (members only)")
    @GetMapping("/organizations/{id}/athletes")
    public ResponseEntity<List<AthleteAffiliationDto>> athletes(
            @PathVariable String id, @RequestParam(defaultValue = "ACTIVE") String status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.listAthletes(
                id, status.toUpperCase(), principal.userId(), principal.roles()));
    }

    @Operation(summary = "The caller's own organization affiliations, as an athlete")
    @GetMapping("/organization-affiliations")
    public ResponseEntity<List<AthleteAffiliationDto>> myAffiliations(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.listMyAffiliations(principal.userId()));
    }

    @Operation(summary = "The athlete approves a pending affiliation")
    @PostMapping("/organization-affiliations/{id}/approve")
    public ResponseEntity<AthleteAffiliationDto> approveAffiliation(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.decideAffiliation(id, principal.userId(), true));
    }

    @Operation(summary = "The athlete rejects a pending affiliation")
    @PostMapping("/organization-affiliations/{id}/reject")
    public ResponseEntity<AthleteAffiliationDto> rejectAffiliation(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.decideAffiliation(id, principal.userId(), false));
    }

    @Operation(summary = "End a pending or active affiliation (the athlete, or an organization manager)")
    @PostMapping("/organization-affiliations/{id}/end")
    public ResponseEntity<AthleteAffiliationDto> endAffiliation(
            @PathVariable String id, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(organizationService.endAffiliation(id, principal.userId(), principal.roles()));
    }
}
