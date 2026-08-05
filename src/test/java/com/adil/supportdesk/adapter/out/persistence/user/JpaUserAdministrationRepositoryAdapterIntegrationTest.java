package com.adil.supportdesk.adapter.out.persistence.user;

import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.port.out.UserAccountPage;
import com.adil.supportdesk.application.port.out.UserAdministrationRepository;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class JpaUserAdministrationRepositoryAdapterIntegrationTest {

    private static final Instant BASE_TIME =
            Instant.parse("2026-08-03T00:00:00Z");

    @Autowired
    private UserAdministrationRepository userRepository;

    @Test
    @DisplayName(
            "Saving existing user should replace roles "
                    + "and preserve token version"
    )
    void shouldReplaceExistingUserRoles() {
        String token = uniqueToken();

        AuthUser originalUser = newUser(
                token + "-replace",
                Set.of(UserRole.USER),
                BASE_TIME
        );

        AuthUser savedUser =
                userRepository.save(originalUser);

        assertEquals(
                0L,
                savedUser.tokenVersion()
        );

        AuthUser updatedUser =
                savedUser.withRoles(
                        Set.of(UserRole.AGENT)
                );

        assertEquals(
                1L,
                updatedUser.tokenVersion()
        );

        AuthUser persistedUser =
                userRepository.save(updatedUser);

        AuthUser reloadedUser = userRepository
                .findById(savedUser.id())
                .orElseThrow();

        assertEquals(
                Set.of(UserRole.AGENT),
                reloadedUser.roles()
        );

        assertFalse(
                reloadedUser.roles()
                        .contains(UserRole.USER)
        );

        assertEquals(
                1L,
                persistedUser.tokenVersion()
        );

        assertEquals(
                1L,
                reloadedUser.tokenVersion()
        );
    }

    @Test
    @DisplayName(
            "User list should filter by role and email"
    )
    void shouldFilterUsersByRoleAndEmail() {
        String token = uniqueToken();

        userRepository.save(
                newUser(
                        token + "-agent-one",
                        Set.of(UserRole.AGENT),
                        BASE_TIME
                )
        );

        userRepository.save(
                newUser(
                        token + "-agent-two",
                        Set.of(
                                UserRole.USER,
                                UserRole.AGENT
                        ),
                        BASE_TIME.plusSeconds(60)
                )
        );

        userRepository.save(
                newUser(
                        token + "-regular-user",
                        Set.of(UserRole.USER),
                        BASE_TIME.plusSeconds(120)
                )
        );

        UserAccountPage result =
                userRepository.findAll(
                        UserRole.AGENT,
                        token.toUpperCase(Locale.ROOT),
                        0,
                        20
                );

        assertEquals(
                2,
                result.totalElements()
        );

        assertEquals(
                2,
                result.content().size()
        );

        assertTrue(
                result.content()
                        .stream()
                        .allMatch(user ->
                                user.roles()
                                        .contains(
                                                UserRole.AGENT
                                        )
                        )
        );

        assertTrue(
                result.content()
                        .stream()
                        .allMatch(user ->
                                user.email()
                                        .contains(token)
                        )
        );
    }

    @Test
    @DisplayName(
            "User list should support stable pagination"
    )
    void shouldPaginateUsers() {
        String token = uniqueToken();

        AuthUser oldestUser = userRepository.save(
                newUser(
                        token + "-oldest",
                        Set.of(UserRole.USER),
                        BASE_TIME
                )
        );

        AuthUser middleUser = userRepository.save(
                newUser(
                        token + "-middle",
                        Set.of(UserRole.USER),
                        BASE_TIME.plusSeconds(60)
                )
        );

        AuthUser newestUser = userRepository.save(
                newUser(
                        token + "-newest",
                        Set.of(UserRole.USER),
                        BASE_TIME.plusSeconds(120)
                )
        );

        UserAccountPage firstPage =
                userRepository.findAll(
                        null,
                        token,
                        0,
                        2
                );

        UserAccountPage secondPage =
                userRepository.findAll(
                        null,
                        token,
                        1,
                        2
                );

        assertEquals(
                3,
                firstPage.totalElements()
        );

        assertEquals(
                2,
                firstPage.totalPages()
        );

        assertEquals(
                2,
                firstPage.content().size()
        );

        assertTrue(firstPage.first());
        assertFalse(firstPage.last());

        assertEquals(
                newestUser.id(),
                firstPage.content()
                        .get(0)
                        .id()
        );

        assertEquals(
                middleUser.id(),
                firstPage.content()
                        .get(1)
                        .id()
        );

        assertEquals(
                1,
                secondPage.content().size()
        );

        assertFalse(secondPage.first());
        assertTrue(secondPage.last());

        assertEquals(
                oldestUser.id(),
                secondPage.content()
                        .get(0)
                        .id()
        );
    }

    private AuthUser newUser(
            String emailPrefix,
            Set<UserRole> roles,
            Instant createdAt
    ) {
        return new AuthUser(
                UserId.generate(),
                emailPrefix
                        + "-"
                        + UUID.randomUUID()
                        + "@integration.test",
                "integration-password-hash",
                "Integration Test User",
                roles,
                createdAt
        );
    }

    private String uniqueToken() {
        return "user-management-"
                + UUID.randomUUID();
    }
}