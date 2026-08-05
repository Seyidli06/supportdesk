package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.domain.ticket.model.TicketEvent;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;

import java.util.List;

public interface TicketEventRepository {

    TicketEvent save(TicketEvent event);

    List<TicketEvent> findByTicketId(
            TicketId ticketId
    );
}