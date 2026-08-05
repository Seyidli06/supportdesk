package com.adil.supportdesk.application.user.management;

import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.port.out.UserAccountPage;
import com.adil.supportdesk.application.port.out.UserAdministrationRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.user.UserNotFoundException;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class UserManagementApplicationServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T00:00:00Z");

    @Mock
    private UserAdministrationRepository userRepository;

    private UserManagementApplicationService service;

    private UserId adminId;
    private UserId userId;

    private AuthUser admin;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        service = new UserManagementApplicationService(
                userRepository
        );

        adminId = UserId.generate();
        userId = UserId.generate();

        admin = new AuthUser(
                adminId,
                "admin@supportdesk.local",
                "admin-password-hash",
                "SupportDesk Admin",
                Set.of(UserRole.ADMIN),
                CREATED_AT
        );

        user = new AuthUser(
                userId,
                "user@supportdesk.local",
                "user-password-hash",
                "SupportDesk User",
                Set.of(UserRole.USER),
                CREATED_AT
        );
    }

    @Test
    @DisplayName(
            "ADMIN should list users"
    )
    void adminShouldListUsers() {
        ListUsersQuery query = new ListUsersQuery(
                null,
                null,
                0,
                20
        );

        UserContext adminContext = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        when(
                userRepository.findAll(
                        null,
                        null,
                        0,
                        20
                )
        ).thenReturn(
                new UserAccountPage(
                        List.of(admin, user),
                        0,
                        20,
                        2,
                        1,
                        true,
                        true
                )
        );

        UserPageResult result = service.listUsers(
                query,
                adminContext
        );

        assertEquals(
                2,
                result.content().size()
        );

        assertEquals(
                2,
                result.totalElements()
        );

        assertEquals(
                adminId.toString(),
                result.content().get(0).id()
        );

        assertEquals(
                userId.toString(),
                result.content().get(1).id()
        );

        verify(userRepository).findAll(
                null,
                null,
                0,
                20
        );
    }

    @Test
    @DisplayName(
            "USER should not list users"
    )
    void userShouldNotListUsers() {
        ListUsersQuery query = new ListUsersQuery(
                null,
                null,
                0,
                20
        );

        UserContext userContext = new UserContext(
                userId.toString(),
                UserRole.USER
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.listUsers(
                        query,
                        userContext
                )
        );

        verify(
                userRepository,
                never()
        ).findAll(
                any(),
                any(),
                any(Integer.class),
                any(Integer.class)
        );
    }

    @Test
    @DisplayName(
            "AGENT should not get user details"
    )
    void agentShouldNotGetUserDetails() {
        UserContext agentContext = new UserContext(
                UserId.generate().toString(),
                UserRole.AGENT
        );

        GetUserQuery query = new GetUserQuery(
                userId.toString()
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> service.getUser(
                        query,
                        agentContext
                )
        );

        verify(
                userRepository,
                never()
        ).findById(any());
    }

    @Test
    @DisplayName(
            "ADMIN should get user details"
    )
    void adminShouldGetUserDetails() {
        UserContext adminContext = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        GetUserQuery query = new GetUserQuery(
                userId.toString()
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserManagementResult result = service.getUser(
                query,
                adminContext
        );

        assertEquals(
                userId.toString(),
                result.id()
        );

        assertEquals(
                "user@supportdesk.local",
                result.email()
        );

        assertEquals(
                Set.of(UserRole.USER),
                result.roles()
        );
    }

    @Test
    @DisplayName(
            "Unknown user should return UserNotFoundException"
    )
    void unknownUserShouldBeRejected() {
        UserContext adminContext = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        GetUserQuery query = new GetUserQuery(
                userId.toString()
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.getUser(
                        query,
                        adminContext
                )
        );
    }

    @Test
    @DisplayName(
            "ADMIN should update USER role to AGENT"
    )
    void adminShouldUpdateUserRoleToAgent() {
        UserContext adminContext = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        UpdateUserRolesCommand command =
                new UpdateUserRolesCommand(
                        userId.toString(),
                        Set.of(UserRole.AGENT)
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(AuthUser.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UserManagementResult result =
                service.updateRoles(
                        command,
                        adminContext
                );

        assertEquals(
                Set.of(UserRole.AGENT),
                result.roles()
        );

        ArgumentCaptor<AuthUser> userCaptor =
                ArgumentCaptor.forClass(
                        AuthUser.class
                );

        verify(userRepository).save(
                userCaptor.capture()
        );

        assertEquals(
                1L,
                userCaptor.getValue().tokenVersion()
        );
    }

    @Test
    @DisplayName(
            "ADMIN should assign multiple roles"
    )
    void adminShouldAssignMultipleRoles() {
        UserContext adminContext = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        Set<UserRole> newRoles = Set.of(
                UserRole.USER,
                UserRole.AGENT
        );

        UpdateUserRolesCommand command =
                new UpdateUserRolesCommand(
                        userId.toString(),
                        newRoles
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(AuthUser.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UserManagementResult result =
                service.updateRoles(
                        command,
                        adminContext
                );

        assertEquals(
                newRoles,
                result.roles()
        );
    }

    @Test
    @DisplayName(
            "ADMIN should not remove ADMIN role from own account"
    )
    void adminShouldNotRemoveOwnAdminRole() {
        UserContext adminContext = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        UpdateUserRolesCommand command =
                new UpdateUserRolesCommand(
                        adminId.toString(),
                        Set.of(UserRole.USER)
                );

        when(userRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        assertThrows(
                SelfAdminRoleRemovalException.class,
                () -> service.updateRoles(
                        command,
                        adminContext
                )
        );

        verify(
                userRepository,
                never()
        ).save(any());
    }

    @Test
    @DisplayName(
            "ADMIN should update another ADMIN account"
    )
    void adminShouldUpdateAnotherAdmin() {
        UserId anotherAdminId = UserId.generate();

        AuthUser anotherAdmin = new AuthUser(
                anotherAdminId,
                "admin2@supportdesk.local",
                "password-hash",
                "Second Admin",
                Set.of(UserRole.ADMIN),
                CREATED_AT
        );

        UserContext adminContext = new UserContext(
                adminId.toString(),
                UserRole.ADMIN
        );

        UpdateUserRolesCommand command =
                new UpdateUserRolesCommand(
                        anotherAdminId.toString(),
                        Set.of(UserRole.AGENT)
                );

        when(
                userRepository.findById(
                        anotherAdminId
                )
        ).thenReturn(Optional.of(anotherAdmin));

        when(userRepository.save(any(AuthUser.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UserManagementResult result =
                service.updateRoles(
                        command,
                        adminContext
                );

        assertEquals(
                Set.of(UserRole.AGENT),
                result.roles()
        );
    }

    @Test
    @DisplayName(
            "Empty role set should be rejected"
    )
    void emptyRoleSetShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new UpdateUserRolesCommand(
                        userId.toString(),
                        Set.of()
                )
        );
    }
}