package com.adil.supportdesk.domain.ticket.exception;


public class TicketClosedException extends DomainException {
    public TicketClosedException(String action) {
        super(String.format("Cannot perform action '%s' on a closed ticket", action));
    }
}