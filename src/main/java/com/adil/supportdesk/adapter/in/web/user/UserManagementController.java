package com.adil.supportdesk.adapter.in.web.user;

import com.adil.supportdesk.adapter.in.web.dto.UpdateUserRolesRequest;
import com.adil.supportdesk.adapter.in.web.dto.UserPageResponse;
import com.adil.supportdesk.adapter.in.web.dto.UserResponse;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.user.management.GetUserQuery;
import com.adil.supportdesk.application.user.management.ListUsersQuery;
import com.adil.supportdesk.application.user.management.UpdateUserRolesCommand;
import com.adil.supportdesk.application.user.management.UserManagementResult;
import com.adil.supportdesk.application.user.management.UserManagementUseCase;
import com.adil.supportdesk.application.user.management.UserPageResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserManagementController {

    private final UserManagementUseCase
            userManagementUseCase;

    public UserManagementController(
            UserManagementUseCase userManagementUseCase
    ) {
        this.userManagementUseCase =
                userManagementUseCase;
    }

    @GetMapping
    public ResponseEntity<UserPageResponse> listUsers(
            @RequestParam(required = false)
            String role,

            @RequestParam(required = false)
            String email,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            Authentication authentication
    ) {
        ListUsersQuery query = new ListUsersQuery(
                parseOptionalRole(role),
                email,
                page,
                size
        );

        UserPageResult result =
                userManagementUseCase.listUsers(
                        query,
                        createUserContext(authentication)
                );

        return ResponseEntity.ok(
                UserPageResponse.fromResult(result)
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable String userId,
            Authentication authentication
    ) {
        GetUserQuery query = new GetUserQuery(
                userId
        );

        UserManagementResult result =
                userManagementUseCase.getUser(
                        query,
                        createUserContext(authentication)
                );

        return ResponseEntity.ok(
                UserResponse.fromResult(result)
        );
    }

    @PatchMapping("/{userId}/roles")
    public ResponseEntity<UserResponse> updateRoles(
            @PathVariable String userId,
            @Valid @RequestBody
            UpdateUserRolesRequest request,
            Authentication authentication
    ) {
        Set<UserRole> roles = request.roles()
                .stream()
                .map(this::parseRequiredRole)
                .collect(Collectors.toUnmodifiableSet());

        UpdateUserRolesCommand command =
                new UpdateUserRolesCommand(
                        userId,
                        roles
                );

        UserManagementResult result =
                userManagementUseCase.updateRoles(
                        command,
                        createUserContext(authentication)
                );

        return ResponseEntity.ok(
                UserResponse.fromResult(result)
        );
    }

    private UserRole parseOptionalRole(
            String role
    ) {
        if (role == null || role.isBlank()) {
            return null;
        }

        return parseRequiredRole(role);
    }

    private UserRole parseRequiredRole(
            String role
    ) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Role cannot be empty"
            );
        }

        try {
            return UserRole.valueOf(
                    role.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Role must be USER, AGENT or ADMIN"
            );
        }
    }

    private UserContext createUserContext(
            Authentication authentication
    ) {
        Set<UserRole> roles = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority ->
                        authority.startsWith("ROLE_")
                )
                .map(authority ->
                        authority.substring(
                                "ROLE_".length()
                        )
                )
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());

        UserRole effectiveRole;

        if (roles.contains(UserRole.ADMIN)) {
            effectiveRole = UserRole.ADMIN;
        } else if (roles.contains(UserRole.AGENT)) {
            effectiveRole = UserRole.AGENT;
        } else {
            effectiveRole = UserRole.USER;
        }

        return new UserContext(
                authentication.getName(),
                effectiveRole
        );
    }
}