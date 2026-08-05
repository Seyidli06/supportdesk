package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.application.ticket.assign.AssignTicketUseCase;
import com.adil.supportdesk.application.ticket.changepriority.ChangeTicketPriorityUseCase;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusUseCase;
import com.adil.supportdesk.application.ticket.comment.AddCommentUseCase;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.get.GetTicketUseCase;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.application.ticket.query.ListTicketsUseCase;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class ChangeTicketPriorityControllerTest {

    private static final String ADMIN_ID =
            "11111111-1111-1111-1111-111111111111";

    private static final String REQUESTER_ID =
            "22222222-2222-2222-2222-222222222222";

    private static final String TICKET_ID =
            "33333333-3333-3333-3333-333333333333";

    private static final Instant CREATED_AT =
            Instant.parse(
                    "2026-08-05T10:00:00Z"
            );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateTicketUseCase
            createTicketUseCase;

    @MockBean
    private AssignTicketUseCase
            assignTicketUseCase;

    @MockBean
    private AddCommentUseCase
            addCommentUseCase;

    @MockBean
    private ChangeTicketStatusUseCase
            changeTicketStatusUseCase;

    @MockBean
    private ChangeTicketPriorityUseCase
            changeTicketPriorityUseCase;

    @MockBean
    private GetTicketUseCase
            getTicketUseCase;

    @MockBean
    private ListTicketsUseCase
            listTicketsUseCase;

    @Test
    @WithMockUser(
            username = ADMIN_ID,
            roles = "ADMIN"
    )
    @DisplayName(
            "Valid priority request should return "
                    + "updated ticket"
    )
    void validPriorityShouldReturnUpdatedTicket()
            throws Exception {

        TicketResult result =
                new TicketResult(
                        TICKET_ID,
                        "Production service unavailable",
                        "The production API is not responding",
                        TicketPriority.URGENT,
                        TicketStatus.OPEN,
                        REQUESTER_ID,
                        null,
                        List.of(),
                        CREATED_AT,
                        CREATED_AT.plusSeconds(60),
                        null,
                        null,
                        null
                );

        when(
                changeTicketPriorityUseCase
                        .changePriority(
                                any(),
                                any()
                        )
        ).thenReturn(result);

        mockMvc.perform(
                        patch(
                                "/api/v1/tickets/{ticketId}/priority",
                                TICKET_ID
                        )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "priority": "URGENT"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(TICKET_ID)
                )
                .andExpect(
                        jsonPath("$.priority")
                                .value("URGENT")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("OPEN")
                )
                .andExpect(
                        jsonPath("$.updatedAt")
                                .exists()
                );
    }

    @Test
    @WithMockUser(
            username = ADMIN_ID,
            roles = "ADMIN"
    )
    @DisplayName(
            "Invalid priority should return 400"
    )
    void invalidPriorityShouldReturn400()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/v1/tickets/{ticketId}/priority",
                                TICKET_ID
                        )
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "priority": "CRITICAL"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                changeTicketPriorityUseCase
        );
    }
}