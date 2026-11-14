package com.jovine360.talenthub.identity.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

/**
 * Account administration for platform admins.
 *
 * <p>Rules: only a SUPER_ADMIN may act on an account that holds ADMIN or SUPER_ADMIN.
 * Nobody may suspend or demote themselves.
 */
public interface UserAdministrationApi {

    /** @param role   optional filter on any held role
     *  @param status optional filter on account status */
    Page<AdminUserDto> listUsers(String role, String status, Pageable pageable);

    AdminUserDto getUser(String userId);

    /** Sets SUSPENDED and revokes all refresh tokens. */
    AdminUserDto suspend(String targetUserId, String actorId, Collection<String> actorRoles);

    AdminUserDto reactivate(String targetUserId, String actorId, Collection<String> actorRoles);

    /**
     * SUPER_ADMIN only: replace the target's primary role. Additional roles are kept, except that
     * removing ADMIN or SUPER_ADMIN as primary also removes it from the additional roles.
     */
    AdminUserDto changePrimaryRole(String targetUserId, String newRole, String actorId);

    /** SUPER_ADMIN only: create an ACTIVE, email-verified ADMIN account. */
    AdminUserDto createAdmin(String email, String password, String actorId);
}
