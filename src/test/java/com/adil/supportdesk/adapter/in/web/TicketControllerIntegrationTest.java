package com.adil.supportdesk.adapter.in.web;

import com.adil.supportdesk.adapter.in.web.dto.CreateTicketRequest;
import com.adil.supportdesk.adapter.in.web.ticket.TicketController;
import com.adil.supportdesk.application.ticket.create.CreateTicketUseCase;
import com.adil.supportdesk.application.ticket.get.TicketResult;
import com.adil.supportdesk.domain.ticket.model.TicketStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTicketUseCase createTicketUseCase;

    @Test
    @WithMockUser
    @DisplayName("Düzgün parametrlərlə POST /api/v1/tickets çağırıldıqda 201 Created qaytarmalıdır")
    void shouldCreateTicketSuccessfully() throws Exception {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("Sistem xətası", "Təsvir yazısı");

        TicketResult mockResult = new TicketResult(
                UUID.randomUUID().toString(),
                "Sistem xətası",
                "Təsvir yazısı",
                TicketStatus.OPEN,
                List.of(),
                Instant.now(),
                Instant.now()
        );

        given(createTicketUseCase.createTicket(any())).willReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/v1/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockResult.id()))
                .andExpect(jsonPath("$.title").value("Sistem xətası"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @WithMockUser
    @DisplayName("Başlıq boş olduqda Validation xətası baş verməli və 400 Bad Request qaytarmalıdır")
    void shouldReturn400WhenTitleIsEmpty() throws Exception {
        // Given
        CreateTicketRequest invalidRequest = new CreateTicketRequest("", "Açıqlama təsviri");

        // When & Then
        mockMvc.perform(post("/api/v1/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}