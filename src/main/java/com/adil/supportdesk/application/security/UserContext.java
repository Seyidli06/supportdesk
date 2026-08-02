package com.adil.supportdesk.application.security;

public record UserContext(
        String userId,
        UserRole role
) {
    public boolean isAgentOrAdmin() {
        return role == UserRole.AGENT || role == UserRole.ADMIN;
    }
}