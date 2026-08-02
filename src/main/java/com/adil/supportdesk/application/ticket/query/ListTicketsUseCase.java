package com.adil.supportdesk.application.ticket.query;

import com.adil.supportdesk.application.security.UserContext;

public interface ListTicketsUseCase {

    TicketPageResult listTickets(
            ListTicketsQuery query,
            UserContext userContext
    );
}