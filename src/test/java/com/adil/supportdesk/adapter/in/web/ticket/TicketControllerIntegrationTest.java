package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.adapter.in.web.ticket.dto.CreateTicketRequest;
import com.adil.supportdesk.adapter.in.web.ticket.TicketController;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adil.supportdesk.application.ticket.assign.AssignTicketUseCase;

import com.adil.supportdesk.application.ticket.comment.AddCommentUseCase;

import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusUseCase;

import com.adil.supportdesk.application.ticket.get.GetTicketUseCase;
import com.adil.supportdesk.application.ticket.query.ListTicketsUseCase;
import com.adil.supportdesk.application.ticket.changepriority.ChangeTicketPriorityUseCase;

@WebMvcTest(TicketController.class)
class TicketControllerIntegrationTest {

    @MockBean
    private GetTicketUseCase getTicketUseCase;

    @MockBean
    private ListTicketsUseCase listTicketsUseCase;

    @MockBean
    private ChangeTicketStatusUseCase changeTicketStatusUseCase;

    @MockBean
    private AddCommentUseCase addCommentUseCase;

    @MockBean
    private AssignTicketUseCase assignTicketUseCase;

    @MockBean
    private ChangeTicketPriorityUseCase
            changeTicketPriorityUseCase;

    private static final String USER_ID =
            "11111111-1111-1111-1111-111111111111";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTicketUseCase createTicketUseCase;

    @Test
    @WithMockUser(
            username = USER_ID,
            roles = "USER"
    )
    @DisplayName(
            "Düzgün parametrlərlə ticket yaradıldıqda 201 qaytarmalıdır"
    )
    void shouldCreateTicketSuccessfully() throws Exception {
        CreateTicketRequest request =
                new CreateTicketRequest(
                        "Sistem xətası",
                        "Sistemdə giriş zamanı xəta baş verir",
                        "HIGH"
                );

        Instant createdAt =
                Instant.parse("2026-08-02T12:00:00Z");

        TicketResult mockResult =
                new TicketResult(
                        UUID.randomUUID().toString(),
                        "Sistem xətası",
                        "Sistemdə giriş zamanı xəta baş verir",
                        TicketPriority.HIGH,
                        TicketStatus.OPEN,
                        USER_ID,
                        null,
                        List.of(),
                        createdAt,
                        createdAt,
                        null,
                        null,
                        null
                );

        given(
                createTicketUseCase.createTicket(
                        any(),
                        any()
                )
        ).willReturn(mockResult);

        mockMvc.perform(
                        post("/api/v1/tickets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(mockResult.id())
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Sistem xətası")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value(
                                        "Sistemdə giriş zamanı xəta baş verir"
                                )
                )
                .andExpect(
                        jsonPath("$.priority")
                                .value("HIGH")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("OPEN")
                )
                .andExpect(
                        jsonPath("$.requesterId")
                                .value(USER_ID)
                )
                .andExpect(
                        jsonPath("$.assignedAgentId")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.createdAt")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.updatedAt")
                                .exists()
                );
    }

    @Test
    @WithMockUser(
            username = USER_ID,
            roles = "USER"
    )
    @DisplayName(
            "Başlıq boş olduqda 400 Bad Request qaytarmalıdır"
    )
    void shouldReturn400WhenTitleIsEmpty() throws Exception {
        CreateTicketRequest invalidRequest =
                new CreateTicketRequest(
                        "",
                        "Açıqlama təsviri",
                        "HIGH"
                );

        mockMvc.perform(
                        post("/api/v1/tickets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(
            username = USER_ID,
            roles = "USER"
    )
    @DisplayName(
            "Açıqlama boş olduqda 400 Bad Request qaytarmalıdır"
    )
    void shouldReturn400WhenDescriptionIsEmpty()
            throws Exception {

        CreateTicketRequest invalidRequest =
                new CreateTicketRequest(
                        "Sistem xətası",
                        "",
                        "HIGH"
                );

        mockMvc.perform(
                        post("/api/v1/tickets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(
            username = USER_ID,
            roles = "USER"
    )
    @DisplayName(
            "Priority yanlış olduqda 400 Bad Request qaytarmalıdır"
    )
    void shouldReturn400WhenPriorityIsInvalid()
            throws Exception {

        CreateTicketRequest invalidRequest =
                new CreateTicketRequest(
                        "Sistem xətası",
                        "Açıqlama təsviri",
                        "CRITICAL"
                );

        mockMvc.perform(
                        post("/api/v1/tickets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName(
            "Authentication olmadan ticket yaradılması bloklanmalıdır"
    )
    void shouldRejectUnauthenticatedRequest()
            throws Exception {

        CreateTicketRequest request =
                new CreateTicketRequest(
                        "Sistem xətası",
                        "Açıqlama təsviri",
                        "HIGH"
                );

        mockMvc.perform(
                        post("/api/v1/tickets")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());
    }
}