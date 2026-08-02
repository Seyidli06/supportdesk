package com.adil.supportdesk.infrastructure.config;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusApplicationService;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusUseCase;
import com.adil.supportdesk.application.ticket.create.CreateTicketApplicationService;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TicketUseCaseConfig {

    @Bean
    public CreateTicketUseCase createTicketUseCase(TicketRepository ticketRepository) {
        return new CreateTicketApplicationService(ticketRepository);
    }

    @Bean
    public ChangeTicketStatusUseCase changeTicketStatusUseCase(TicketRepository ticketRepository) {
        return new ChangeTicketStatusApplicationService(ticketRepository);
    }
}