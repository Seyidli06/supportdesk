package com.adil.supportdesk.adapter.in.web.user;

import com.adil.supportdesk.adapter.in.web.error.GlobalExceptionHandler;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.user.management.GetUserQuery;
import com.adil.supportdesk.application.user.management.ListUsersQuery;
import com.adil.supportdesk.application.user.management.UpdateUserRolesCommand;
import com.adil.supportdesk.application.user.management.UserManagementResult;
import com.adil.supportdesk.application.user.management.UserManagementUseCase;
import com.adil.supportdesk.application.user.management.UserPageResult;
import com.adil.supportdesk.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManagementController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class UserManagementControllerIntegrationTest {

    private static final String ADMIN_ID =
            "11111111-1111-1111-1111-111111111111";

    private static final String USER_ID =
            "22222222-2222-2222-2222-222222222222";

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementUseCase
            userManagementUseCase;

    @Test
    @DisplayName(
            "Unauthenticated request should return 401 Problem Details"
    )
    void unauthenticatedRequestShouldReturn401()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.type")
                                .value(
                                        "https://supportdesk.com/errors/"
                                                + "authentication-required"
                                )
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Authentication Required"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "Authentication is required "
                                                + "to access this resource"
                                )
                )
                .andExpect(
                        jsonPath("$.instance")
                                .value("/api/v1/users")
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );

        verifyNoInteractions(
                userManagementUseCase
        );
    }


    @Test
    @WithMockUser(
            username = USER_ID,
            roles = "USER"
    )
    @DisplayName(
            "USER should receive 403 Problem Details"
    )
    void userShouldReceive403()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(
                        status().isForbidden()
                )
                .andExpect(
                        jsonPath("$.type")
                                .value(
                                        "https://supportdesk.com/errors/"
                                                + "access-denied"
                                )
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Access Denied")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "You do not have permission "
                                                + "to access this resource"
                                )
                )
                .andExpect(
                        jsonPath("$.instance")
                                .value("/api/v1/users")
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );

        verifyNoInteractions(
                userManagementUseCase
        );
    }

    @Test
    @WithMockUser(
            username = USER_ID,
            roles = "AGENT"
    )
    @DisplayName(
            "AGENT should receive 403"
    )
    void agentShouldReceive403()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(
                        status().isForbidden()
                );

        verifyNoInteractions(
                userManagementUseCase
        );
    }

    @Test
    @WithMockUser(
            username = ADMIN_ID,
            roles = "ADMIN"
    )
    @DisplayName(
            "ADMIN should list users"
    )
    void adminShouldListUsers()
            throws Exception {

        UserManagementResult admin =
                new UserManagementResult(
                        ADMIN_ID,
                        "admin@supportdesk.local",
                        "SupportDesk Admin",
                        Set.of(UserRole.ADMIN),
                        CREATED_AT
                );

        UserManagementResult agent =
                new UserManagementResult(
                        USER_ID,
                        "agent@supportdesk.local",
                        "Support Agent",
                        Set.of(UserRole.AGENT),
                        CREATED_AT
                );

        when(
                userManagementUseCase.listUsers(
                        any(ListUsersQuery.class),
                        any(UserContext.class)
                )
        ).thenReturn(
                new UserPageResult(
                        List.of(admin, agent),
                        0,
                        20,
                        2,
                        1,
                        true,
                        true
                )
        );

        mockMvc.perform(
                        get("/api/v1/users")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(ADMIN_ID)
                )
                .andExpect(
                        jsonPath("$.content[0].roles[0]")
                                .value("ADMIN")
                )
                .andExpect(
                        jsonPath("$.content[1].roles[0]")
                                .value("AGENT")
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(2)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].passwordHash"
                        ).doesNotExist()
                );
    }

    @Test
    @WithMockUser(
            username = ADMIN_ID,
            roles = "ADMIN"
    )
    @DisplayName(
            "ADMIN should get user details"
    )
    void adminShouldGetUserDetails()
            throws Exception {

        when(
                userManagementUseCase.getUser(
                        any(GetUserQuery.class),
                        any(UserContext.class)
                )
        ).thenReturn(
                new UserManagementResult(
                        USER_ID,
                        "user@supportdesk.local",
                        "SupportDesk User",
                        Set.of(UserRole.USER),
                        CREATED_AT
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/users/{userId}",
                                USER_ID
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(USER_ID)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "user@supportdesk.local"
                                )
                )
                .andExpect(
                        jsonPath("$.roles[0]")
                                .value("USER")
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                );
    }

    @Test
    @WithMockUser(
            username = ADMIN_ID,
            roles = "ADMIN"
    )
    @DisplayName(
            "ADMIN should update user roles"
    )
    void adminShouldUpdateUserRoles()
            throws Exception {

        when(
                userManagementUseCase.updateRoles(
                        any(UpdateUserRolesCommand.class),
                        any(UserContext.class)
                )
        ).thenReturn(
                new UserManagementResult(
                        USER_ID,
                        "user@supportdesk.local",
                        "SupportDesk User",
                        Set.of(
                                UserRole.USER,
                                UserRole.AGENT
                        ),
                        CREATED_AT
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/users/{userId}/roles",
                                USER_ID
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "roles": [
                                            "USER",
                                            "AGENT"
                                          ]
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(USER_ID)
                )
                .andExpect(
                        jsonPath("$.roles.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.roles[0]")
                                .value("AGENT")
                )
                .andExpect(
                        jsonPath("$.roles[1]")
                                .value("USER")
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                );
    }

    @Test
    @WithMockUser(
            username = ADMIN_ID,
            roles = "ADMIN"
    )
    @DisplayName(
            "Invalid role should return 400"
    )
    void invalidRoleShouldReturn400()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/users/{userId}/roles",
                                USER_ID
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "roles": [
                                            "SUPER_ADMIN"
                                          ]
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Invalid Request")
                );

        verifyNoInteractions(
                userManagementUseCase
        );
    }
}