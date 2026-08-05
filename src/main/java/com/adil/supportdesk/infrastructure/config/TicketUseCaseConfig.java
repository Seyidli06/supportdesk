package com.adil.supportdesk.infrastructure.config;

import com.adil.supportdesk.application.port.out.TicketMutationRepository;
import com.adil.supportdesk.application.port.out.TicketQueryRepository;
import com.adil.supportdesk.application.port.out.TicketRepository;
import com.adil.supportdesk.application.port.out.UserDirectory;
import com.adil.supportdesk.application.ticket.assign.AssignTicketApplicationService;
import com.adil.supportdesk.application.ticket.assign.AssignTicketUseCase;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusApplicationService;
import com.adil.supportdesk.application.ticket.changestatus.ChangeTicketStatusUseCase;
import com.adil.supportdesk.application.ticket.comment.AddCommentApplicationService;
import com.adil.supportdesk.application.ticket.comment.AddCommentUseCase;
import com.adil.supportdesk.application.ticket.create.CreateTicketApplicationService;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.get.GetTicketApplicationService;
import com.adil.supportdesk.application.ticket.get.GetTicketUseCase;
import com.adil.supportdesk.application.ticket.query.ListTicketsApplicationService;
import com.adil.supportdesk.application.ticket.query.ListTicketsUseCase;
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
            TicketMutationRepository
                    ticketMutationRepository,
            Clock clock
    ) {
        return new CreateTicketApplicationService(
                ticketMutationRepository,
                clock
        );
    }

    @Bean
    public AssignTicketUseCase assignTicketUseCase(
            TicketRepository ticketRepository,
            TicketMutationRepository
                    ticketMutationRepository,
            UserDirectory userDirectory,
            Clock clock
    ) {
        return new AssignTicketApplicationService(
                ticketRepository,
                ticketMutationRepository,
                userDirectory,
                clock
        );
    }

    @Bean
    public AddCommentUseCase addCommentUseCase(
            TicketRepository ticketRepository,
            TicketMutationRepository
                    ticketMutationRepository,
            Clock clock
    ) {
        return new AddCommentApplicationService(
                ticketRepository,
                ticketMutationRepository,
                clock
        );
    }

    @Bean
    public ChangeTicketStatusUseCase
    changeTicketStatusUseCase(
            TicketRepository ticketRepository,
            TicketMutationRepository
                    ticketMutationRepository,
            Clock clock
    ) {
        return new ChangeTicketStatusApplicationService(
                ticketRepository,
                ticketMutationRepository,
                clock
        );
    }

    @Bean
    public GetTicketUseCase getTicketUseCase(
            TicketQueryRepository ticketQueryRepository
    ) {
        return new GetTicketApplicationService(
                ticketQueryRepository
        );
    }

    @Bean
    public ListTicketsUseCase listTicketsUseCase(
            TicketQueryRepository ticketQueryRepository
    ) {
        return new ListTicketsApplicationService(
                ticketQueryRepository
        );
    }
}