package com.adil.supportdesk.infrastructure.config;

import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusApplicationService;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusUseCase;
import com.adil.supportdesk.application.ticket.create.CreateTicketApplicationService;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TicketUseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CreateTicketUseCase createTicketUseCase(
            TicketRepository ticketRepository,
            Clock clock
    ) {
        return new CreateTicketApplicationService(
                ticketRepository,
                clock
        );
    }

    @Bean
    public ChangeTicketStatusUseCase
    changeTicketStatusUseCase(
            TicketRepository ticketRepository
    ) {
        return new ChangeTicketStatusApplicationService(
                ticketRepository
        );
    }
}