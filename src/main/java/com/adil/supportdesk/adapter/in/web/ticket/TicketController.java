package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.adapter.in.web.dto.AssignTicketRequest;
import com.adil.supportdesk.adapter.in.web.dto.CreateTicketRequest;
import com.adil.supportdesk.adapter.in.web.dto.TicketResponse;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.assign.AssignTicketCommand;
import com.adil.supportdesk.application.ticket.assign.AssignTicketUseCase;
import com.adil.supportdesk.application.ticket.create.CreateTicketCommand;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final AssignTicketUseCase assignTicketUseCase;

    public TicketController(
            CreateTicketUseCase createTicketUseCase,
            AssignTicketUseCase assignTicketUseCase
    ) {
        this.createTicketUseCase = createTicketUseCase;
        this.assignTicketUseCase = assignTicketUseCase;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication
    ) {
        CreateTicketCommand command =
                new CreateTicketCommand(
                        request.title(),
                        request.description(),
                        TicketPriority.valueOf(
                                request.priority()
                                        .toUpperCase(Locale.ROOT)
                        )
                );

        UserContext userContext =
                createUserContext(authentication);

        TicketResult result =
                createTicketUseCase.createTicket(
                        command,
                        userContext
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TicketResponse.fromResult(result));
    }

    @PatchMapping("/{ticketId}/assignment")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable String ticketId,
            @Valid @RequestBody AssignTicketRequest request,
            Authentication authentication
    ) {
        AssignTicketCommand command =
                new AssignTicketCommand(
                        ticketId,
                        request.agentId()
                );

        UserContext userContext =
                createUserContext(authentication);

        TicketResult result =
                assignTicketUseCase.assignTicket(
                        command,
                        userContext
                );

        return ResponseEntity.ok(
                TicketResponse.fromResult(result)
        );
    }

    private UserContext createUserContext(
            Authentication authentication
    ) {
        Set<UserRole> roles = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority ->
                        authority.startsWith("ROLE_")
                )
                .map(authority ->
                        authority.substring("ROLE_".length())
                )
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());

        UserRole effectiveRole;

        if (roles.contains(UserRole.ADMIN)) {
            effectiveRole = UserRole.ADMIN;
        } else if (roles.contains(UserRole.AGENT)) {
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