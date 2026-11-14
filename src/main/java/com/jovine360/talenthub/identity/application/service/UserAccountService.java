package com.jovine360.talenthub.identity.application.service;

import com.jovine360.talenthub.identity.api.UserAccountDto;
import com.jovine360.talenthub.identity.api.UserDirectoryApi;
import com.jovine360.talenthub.identity.api.UserRoleGrantApi;
import com.jovine360.talenthub.identity.domain.model.User;
import com.jovine360.talenthub.identity.domain.model.UserRole;
import com.jovine360.talenthub.identity.domain.repository.UserRepository;
import com.jovine360.talenthub.shared.domain.exception.DuplicateResourceException;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Self-service account operations for the authenticated user. */
@Service
@Transactional
public class UserAccountService implements UserDirectoryApi, UserRoleGrantApi {

    private final UserRepository userRepository;

    public UserAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserAccountDto getAccount(String userId) {
        return toDto(load(userId));
    }

    /**
     * Adds a non-privileged role to the caller's account. The new role appears in tokens
     * issued after the next login or refresh.
     */
    public UserAccountDto assignRole(String userId, UserRole role) {
        if (role.isPrivileged()) {
            throw new IllegalArgumentException("Role " + role + " cannot be self-assigned");
        }
        User user = load(userId);
        if (user.hasRole(role)) {
            throw new DuplicateResourceException("User already has role " + role);
        }
        user.getAdditionalRoles().add(role);
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasRole(String userId, String role) {
        return userRepository.findById(userId)
                .map(u -> u.allRoles().stream().anyMatch(r -> r.name().equals(role)))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> rolesOf(String userId) {
        return userRepository.findById(userId)
                .map(u -> u.allRoles().stream().map(Enum::name).toList())
                .orElse(List.of());
    }

    @Override
    public void grantRole(String userId, String role) {
        User user = load(userId);
        UserRole r = UserRole.valueOf(role);
        if (!user.hasRole(r)) {
            user.getAdditionalRoles().add(r);
            userRepository.save(user);
        }
    }

    private User load(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    private UserAccountDto toDto(User u) {
        return new UserAccountDto(u.getId(), u.getEmail(), u.getRole().name(),
                u.allRoles().stream().map(Enum::name).toList(),
                u.getStatus().name(), u.isEmailVerified());
    }
}
