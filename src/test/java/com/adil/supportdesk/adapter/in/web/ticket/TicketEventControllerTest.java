package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.application.ticket.event.GetTicketEventsUseCase;
import com.adil.supportdesk.application.ticket.event.TicketEventResult;
import com.adil.supportdesk.domain.ticket.model.TicketEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketEventController.class)
class TicketEventControllerTest {

    private static final String USER_ID =
            "11111111-1111-1111-1111-111111111111";

    private static final String TICKET_ID =
            "22222222-2222-2222-2222-222222222222";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetTicketEventsUseCase
            getTicketEventsUseCase;

    @Test
    @WithMockUser(
            username = USER_ID,
            roles = "USER"
    )
    @DisplayName(
            "Authorized request should return ticket event history"
    )
    void authorizedRequestShouldReturnEvents()
            throws Exception {

        TicketEventResult event =
                new TicketEventResult(
                        UUID.randomUUID(),
                        TICKET_ID,
                        USER_ID,
                        TicketEventType.TICKET_CREATED,
                        null,
                        "OPEN",
                        Instant.parse(
                                "2026-08-05T10:00:00Z"
                        )
                );

        when(
                getTicketEventsUseCase.getEvents(
                        eq(TICKET_ID),
                        any()
                )
        ).thenReturn(List.of(event));

        mockMvc.perform(
                        get(
                                "/api/v1/tickets/{ticketId}/events",
                                TICKET_ID
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].ticketId")
                                .value(TICKET_ID)
                )
                .andExpect(
                        jsonPath("$[0].actorId")
                                .value(USER_ID)
                )
                .andExpect(
                        jsonPath("$[0].type")
                                .value("TICKET_CREATED")
                )
                .andExpect(
                        jsonPath("$[0].previousValue")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$[0].newValue")
                                .value("OPEN")
                )
                .andExpect(
                        jsonPath("$[0].createdAt")
                                .exists()
                );
    }

    @Test
    @DisplayName(
            "Unauthenticated request should return 401"
    )
    void unauthenticatedRequestShouldReturn401()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/tickets/{ticketId}/events",
                                TICKET_ID
                        )
                )
                .andExpect(status().isUnauthorized());
    }
}