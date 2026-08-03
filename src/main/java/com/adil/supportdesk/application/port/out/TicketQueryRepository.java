package com.adil.supportdesk.application.port.out;

import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.application.ticket.query.TicketPageResult;
import com.adil.supportdesk.application.ticket.query.TicketSearchCriteria;
import com.adil.supportdesk.domain.ticket.valueobject.TicketId;

import java.util.Optional;

public interface TicketQueryRepository {

    Optional<TicketResult> findDetailsById(
            TicketId ticketId
    );

    TicketPageResult findPage(
            TicketSearchCriteria criteria
    );
}