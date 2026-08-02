package com.adil.supportdesk.domain.ticket.exception;

public class InvalidTicketStatusException extends DomainException {
    public InvalidTicketStatusException(String message) {
        super(message);
    }
}