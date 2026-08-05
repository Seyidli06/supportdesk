package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.adapter.in.web.ticket.dto.TicketEventResponse;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.event.GetTicketEventsUseCase;
import com.adil.supportdesk.application.ticket.event.TicketEventResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketEventController {

    private final GetTicketEventsUseCase
            getTicketEventsUseCase;

    public TicketEventController(
            GetTicketEventsUseCase
                    getTicketEventsUseCase
    ) {
        this.getTicketEventsUseCase =
                getTicketEventsUseCase;
    }

    @GetMapping("/{ticketId}/events")
    public ResponseEntity<List<TicketEventResponse>>
    getTicketEvents(
            @PathVariable String ticketId,
            Authentication authentication
    ) {
        List<TicketEventResult> results =
                getTicketEventsUseCase.getEvents(
                        ticketId,
                        createUserContext(
                                authentication
                        )
                );

        List<TicketEventResponse> response =
                results.stream()
                        .map(
                                TicketEventResponse
                                        ::fromResult
                        )
                        .toList();

        return ResponseEntity.ok(response);
    }

    private UserContext createUserContext(
            Authentication authentication
    ) {
        Set<UserRole> roles = authentication
                .getAuthorities()
                .stream()
                .map(
                        GrantedAuthority::getAuthority
                )
                .filter(authority ->
                        authority.startsWith(
                                "ROLE_"
                        )
                )
                .map(authority ->
                        authority.substring(
                                "ROLE_".length()
                        )
                )
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());

        UserRole effectiveRole;

        if (roles.contains(UserRole.ADMIN)) {
            effectiveRole = UserRole.ADMIN;
        } else if (
                roles.contains(UserRole.AGENT)
        ) {
            effectiveRole = UserRole.AGENT;
        } else {
            effectiveRole = UserRole.USER;
        }

        return new UserContext(
                authentication.getName(),
                effectiveRole
        );
    }
}