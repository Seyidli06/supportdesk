package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.domain.ticket.model.Ticket;
import com.adil.supportdesk.domain.ticket.model.TicketEvent;

public interface TicketMutationRepository {

    Ticket saveWithEvent(
            Ticket ticket,
            TicketEvent event
    );
}