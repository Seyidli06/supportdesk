package com.adil.supportdesk.application.ticket.event;

import com.adil.supportdesk.application.security.UserContext;

import java.util.List;

public interface GetTicketEventsUseCase {

    List<TicketEventResult> getEvents(
            String ticketId,
            UserContext userContext
    );
}