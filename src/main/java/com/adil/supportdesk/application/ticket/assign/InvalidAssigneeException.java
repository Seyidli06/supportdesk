package com.adil.supportdesk.application.ticket.assign;

public class InvalidAssigneeException extends RuntimeException {

    public InvalidAssigneeException(String userId) {
        super(
                "User with ID "
                        + userId
                        + " does not have AGENT role"
        );
    }
}