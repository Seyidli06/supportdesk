package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.adapter.in.web.ticket.dto.AddCommentRequest;
import com.adil.supportdesk.adapter.in.web.ticket.dto.AssignTicketRequest;
import com.adil.supportdesk.adapter.in.web.ticket.dto.ChangeTicketStatusRequest;
import com.adil.supportdesk.adapter.in.web.ticket.dto.CreateTicketRequest;
import com.adil.supportdesk.adapter.in.web.ticket.dto.TicketPageResponse;
import com.adil.supportdesk.adapter.in.web.ticket.dto.TicketResponse;
import com.adil.supportdesk.application.security.UserContext;
import com.adil.supportdesk.application.security.UserRole;
import com.adil.supportdesk.application.ticket.assign.AssignTicketCommand;
import com.adil.supportdesk.application.ticket.assign.AssignTicketUseCase;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusCommand;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusUseCase;
import com.adil.supportdesk.application.ticket.comment.AddCommentCommand;
import com.adil.supportdesk.application.ticket.comment.AddCommentUseCase;
import com.adil.supportdesk.application.ticket.create.CreateTicketCommand;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.get.GetTicketUseCase;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.application.ticket.query.ListTicketsQuery;
import com.adil.supportdesk.application.ticket.query.ListTicketsUseCase;
import com.adil.supportdesk.application.ticket.query.TicketPageResult;
import com.adil.supportdesk.domain.ticket.model.TicketPriority;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;
    private final AssignTicketUseCase assignTicketUseCase;
    private final AddCommentUseCase addCommentUseCase;
    private final ChangeTicketStatusUseCase
            changeTicketStatusUseCase;
    private final GetTicketUseCase getTicketUseCase;
    private final ListTicketsUseCase listTicketsUseCase;

    public TicketController(
            CreateTicketUseCase createTicketUseCase,
            AssignTicketUseCase assignTicketUseCase,
            AddCommentUseCase addCommentUseCase,
            ChangeTicketStatusUseCase changeTicketStatusUseCase,
            GetTicketUseCase getTicketUseCase,
            ListTicketsUseCase listTicketsUseCase
    ) {
        this.createTicketUseCase = createTicketUseCase;
        this.assignTicketUseCase = assignTicketUseCase;
        this.addCommentUseCase = addCommentUseCase;
        this.changeTicketStatusUseCase =
                changeTicketStatusUseCase;
        this.getTicketUseCase = getTicketUseCase;
        this.listTicketsUseCase = listTicketsUseCase;
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

        TicketResult result =
                createTicketUseCase.createTicket(
                        command,
                        createUserContext(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TicketResponse.fromResult(result));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable String ticketId,
            Authentication authentication
    ) {
        TicketResult result =
                getTicketUseCase.getTicket(
                        ticketId,
                        createUserContext(authentication)
                );

        return ResponseEntity.ok(
                TicketResponse.fromResult(result)
        );
    }

    @GetMapping
    public ResponseEntity<TicketPageResponse> listTickets(
            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            String priority,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            Authentication authentication
    ) {
        ListTicketsQuery query =
                new ListTicketsQuery(
                        parseStatus(status),
                        parsePriority(priority),
                        page,
                        size
                );

        TicketPageResult result =
                listTicketsUseCase.listTickets(
                        query,
                        createUserContext(authentication)
                );

        return ResponseEntity.ok(
                TicketPageResponse.fromResult(result)
        );
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

        TicketResult result =
                assignTicketUseCase.assignTicket(
                        command,
                        createUserContext(authentication)
                );

        return ResponseEntity.ok(
                TicketResponse.fromResult(result)
        );
    }

    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<TicketResponse> addComment(
            @PathVariable String ticketId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication
    ) {
        AddCommentCommand command =
                new AddCommentCommand(
                        ticketId,
                        request.content()
                );

        TicketResult result =
                addCommentUseCase.addComment(
                        command,
                        createUserContext(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TicketResponse.fromResult(result));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> changeStatus(
            @PathVariable String ticketId,
            @Valid @RequestBody
            ChangeTicketStatusRequest request,
            Authentication authentication
    ) {
        ChangeTicketStatusCommand command =
                new ChangeTicketStatusCommand(
                        ticketId,
                        TicketStatus.valueOf(
                                request.status()
                                        .toUpperCase(Locale.ROOT)
                        )
                );

        TicketResult result =
                changeTicketStatusUseCase.changeStatus(
                        command,
                        createUserContext(authentication)
                );

        return ResponseEntity.ok(
                TicketResponse.fromResult(result)
        );
    }

    private TicketStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return TicketStatus.valueOf(
                status.trim()
                        .toUpperCase(Locale.ROOT)
        );
    }

    private TicketPriority parsePriority(
            String priority
    ) {
        if (
                priority == null
                        || priority.isBlank()
        ) {
            return null;
        }

        return TicketPriority.valueOf(
                priority.trim()
                        .toUpperCase(Locale.ROOT)
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