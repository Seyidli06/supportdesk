package com.adil.supportdesk.adapter.in.web.ticket;

import com.adil.supportdesk.adapter.in.web.dto.CreateTicketRequest;
import com.adil.supportdesk.adapter.in.web.dto.TicketResponse;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.create.CreateTicketCommand;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.Ticket;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final CreateTicketUseCase createTicketUseCase;

    public TicketController(CreateTicketUseCase createTicketUseCase) {
        this.createTicketUseCase = createTicketUseCase;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        // 1. Request DTO-nu Application Command-a çeviririk
        CreateTicketCommand command = new CreateTicketCommand(
                request.title(),
                request.description()
        );

        // 2. UseCase-i çağırırıq
        TicketResult ticket = createTicketUseCase.createTicket(command);

        // 3. Domain Model-i Response DTO-ya çevirib 201 Created qaytarırıq
        TicketResponse response = TicketResponse.fromDomain(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}