package com.adil.supportdesk.domain.ticket.exception;

import com.adil.supportdesk.domain.ticket.model.TicketStatus;

public class InvalidStatusTransitionException extends DomainException {
    public InvalidStatusTransitionException(TicketStatus current, TicketStatus target) {
        super(String.format("Cannot transition ticket status from %s to %s", current, target));
    }
}