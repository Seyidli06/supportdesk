package com.adil.supportdesk.application.ticket.get;

import com.adil.supportdesk.application.security.UserContext;

public interface GetTicketUseCase {

    TicketResult getTicket(
            String ticketId,
            UserContext userContext
    );
}