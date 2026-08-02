package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.adapter.in.web.dto.CreateTicketRequest;
import com.adil.supportdesk.adapter.in.web.dto.TicketResponse;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.create.CreateTicketCommand;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;

    public TicketController(
            CreateTicketUseCase createTicketUseCase
    ) {
        this.createTicketUseCase = createTicketUseCase;
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

    private UserContext createUserContext(
            Authentication authentication
    ) {
        UserRole role = authentication
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
                .findFirst()
                .orElse(UserRole.USER);

        return new UserContext(
                authentication.getName(),
                role
        );
    }
}