package com.adil.supportdesk.application.ticket.query;

import com.adil.supportdesk.application.port.out.TicketQueryRepository;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.query.ListTicketsApplicationService;
import com.adil.supportdesk.application.ticket.query.ListTicketsQuery;
import com.adil.supportdesk.application.ticket.query.TicketPageResult;
import com.adil.supportdesk.application.ticket.query.TicketSearchCriteria;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.adil.supportdesk.domain.user.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTicketsApplicationServiceTest {

    @Mock
    private TicketQueryRepository ticketQueryRepository;

    private ListTicketsApplicationService service;

    private UserId actorId;

    @BeforeEach
    void setUp() {
        service = new ListTicketsApplicationService(
                ticketQueryRepository
        );

        actorId = UserId.generate();

        when(
                ticketQueryRepository.findPage(any())
        ).thenReturn(
                new TicketPageResult(
                        List.of(),
                        0,
                        20,
                        0,
                        0,
                        true,
                        true
                )
        );
    }

    @Test
    @DisplayName(
            "User list should be scoped by requester"
    )
    void userListShouldUseRequesterScope() {
        service.listTickets(
                query(),
                new UserContext(
                        actorId.toString(),
                        UserRole.USER
                )
        );

        TicketSearchCriteria criteria =
                captureCriteria();

        assertEquals(
                actorId,
                criteria.requesterId()
        );

        assertNull(criteria.assignedAgentId());
    }

    @Test
    @DisplayName(
            "Agent list should be scoped by assignment"
    )
    void agentListShouldUseAssignmentScope() {
        service.listTickets(
                query(),
                new UserContext(
                        actorId.toString(),
                        UserRole.AGENT
                )
        );

        TicketSearchCriteria criteria =
                captureCriteria();

        assertNull(criteria.requesterId());

        assertEquals(
                actorId,
                criteria.assignedAgentId()
        );
    }

    @Test
    @DisplayName(
            "Admin list should not have ownership scope"
    )
    void adminListShouldNotHaveOwnershipScope() {
        service.listTickets(
                query(),
                new UserContext(
                        actorId.toString(),
                        UserRole.ADMIN
                )
        );

        TicketSearchCriteria criteria =
                captureCriteria();

        assertNull(criteria.requesterId());
        assertNull(criteria.assignedAgentId());

        assertEquals(
                TicketStatus.OPEN,
                criteria.status()
        );

        assertEquals(
                TicketPriority.HIGH,
                criteria.priority()
        );
    }

    private ListTicketsQuery query() {
        return new ListTicketsQuery(
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                0,
                20
        );
    }

    private TicketSearchCriteria captureCriteria() {
        ArgumentCaptor<TicketSearchCriteria> captor =
                ArgumentCaptor.forClass(
                        TicketSearchCriteria.class
                );

        verify(ticketQueryRepository)
                .findPage(captor.capture());

        return captor.getValue();
    }
}