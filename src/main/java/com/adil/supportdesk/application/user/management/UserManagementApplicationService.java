package com.adil.supportdesk.application.user.management;

import com.adil.supportdesk.application.auth.AuthUser;
import com.adil.supportdesk.application.port.out.UserAccountPage;
import com.adil.supportdesk.application.port.out.UserAdministrationRepository;
import com.adil.supportdesk.application.security.UnauthorizedAccessException;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.user.UserNotFoundException;
import com.adil.supportdesk.domain.user.valueobject.UserId;

import java.util.List;
import java.util.Objects;

public class UserManagementApplicationService
        implements UserManagementUseCase {

    private final UserAdministrationRepository
            userRepository;

    public UserManagementApplicationService(
            UserAdministrationRepository userRepository
    ) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "UserAdministrationRepository cannot be null"
        );
    }

    @Override
    public UserPageResult listUsers(
            ListUsersQuery query,
            UserContext userContext
    ) {
        Objects.requireNonNull(
                query,
                "List users query cannot be null"
        );

        validateAdmin(userContext);

        UserAccountPage page = userRepository.findAll(
                query.role(),
                query.email(),
                query.page(),
                query.size()
        );

        List<UserManagementResult> content =
                page.content()
                        .stream()
                        .map(UserManagementResult::from)
                        .toList();

        return new UserPageResult(
                content,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last()
        );
    }

    @Override
    public UserManagementResult getUser(
            GetUserQuery query,
            UserContext userContext
    ) {
        Objects.requireNonNull(
                query,
                "Get user query cannot be null"
        );

        validateAdmin(userContext);

        UserId userId = UserId.of(
                query.userId()
        );

        AuthUser user = findUser(userId);

        return UserManagementResult.from(user);
    }

    @Override
    public UserManagementResult updateRoles(
            UpdateUserRolesCommand command,
            UserContext userContext
    ) {
        Objects.requireNonNull(
                command,
                "Update roles command cannot be null"
        );

        validateAdmin(userContext);

        UserId targetUserId = UserId.of(
                command.userId()
        );

        UserId authenticatedAdminId = UserId.of(
                userContext.userId()
        );

        AuthUser existingUser = findUser(
                targetUserId
        );

        validateSelfAdminRemoval(
                authenticatedAdminId,
                targetUserId,
                existingUser,
                command
        );

        AuthUser updatedUser = new AuthUser(
                existingUser.id(),
                existingUser.email(),
                existingUser.passwordHash(),
                existingUser.fullName(),
                command.roles(),
                existingUser.createdAt()
        );

        AuthUser savedUser = userRepository.save(
                updatedUser
        );

        return UserManagementResult.from(
                savedUser
        );
    }

    private AuthUser findUser(UserId userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                userId.toString()
                        )
                );
    }

    private void validateAdmin(
            UserContext userContext
    ) {
        Objects.requireNonNull(
                userContext,
                "User context cannot be null"
        );

        if (!userContext.isAdmin()) {
            throw new UnauthorizedAccessException(
                    "Only ADMIN can manage users"
            );
        }
    }

    private void validateSelfAdminRemoval(
            UserId authenticatedAdminId,
            UserId targetUserId,
            AuthUser existingUser,
            UpdateUserRolesCommand command
    ) {
        boolean updatingOwnAccount =
                authenticatedAdminId.equals(
                        targetUserId
                );

        boolean currentlyAdmin =
                existingUser.roles()
                        .contains(UserRole.ADMIN);

        boolean adminRoleRemoved =
                !command.roles()
                        .contains(UserRole.ADMIN);

        if (
                updatingOwnAccount
                        && currentlyAdmin
                        && adminRoleRemoved
        ) {
            throw new SelfAdminRoleRemovalException();
        }
    }
}