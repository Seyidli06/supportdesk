package com.adil.supportdesk.domain.ticket.exception;


public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}