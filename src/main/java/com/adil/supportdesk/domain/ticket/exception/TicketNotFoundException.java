package com.adil.supportdesk.domain.ticket.exception;

public class TicketNotFoundException extends DomainException {
    public TicketNotFoundException(String id) {
        super("Ticket not found with ID: " + id);
    }
}