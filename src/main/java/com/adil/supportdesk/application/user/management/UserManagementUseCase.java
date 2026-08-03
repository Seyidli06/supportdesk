package com.adil.supportdesk.application.user.management;

import com.adil.supportdesk.application.security.UserContext;

public interface UserManagementUseCase {

    UserPageResult listUsers(
            ListUsersQuery query,
            UserContext userContext
    );

    UserManagementResult getUser(
            GetUserQuery query,
            UserContext userContext
    );

    UserManagementResult updateRoles(
            UpdateUserRolesCommand command,
            UserContext userContext
    );
}