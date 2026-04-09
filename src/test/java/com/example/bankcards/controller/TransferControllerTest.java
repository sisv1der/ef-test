package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfiguration;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.exception.CardInactiveException;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.TransferService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import({TestSecurityConfiguration.class, GlobalExceptionHandler.class})
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    private JacksonTester<TransferRequest> transferJacksonTester;

    @MockitoBean
    private TransferService transferService;
    @MockitoBean
    private JwtService jwtService;

    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        JacksonTester.initFields(this, jsonMapper);

        transferRequest = new TransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(1000)
        );
    }

    @Test
    @WithMockUser
    void transfer_shouldReturnOk_whenSuccess() throws Exception {
        doNothing().when(transferService).transfer(any(TransferRequest.class));

        String json = transferJacksonTester.write(transferRequest).getJson();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(transferService).transfer(any(TransferRequest.class));
    }

    @Test
    void transfer_shouldReturnUnauthorized() throws Exception {
        String json = transferJacksonTester.write(transferRequest).getJson();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void transfer_shouldReturnForbidden_whenNotUserRole() throws Exception {
        String json = transferJacksonTester.write(transferRequest).getJson();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void transfer_shouldReturnBadRequest_whenInsufficientFunds() throws Exception {
        doThrow(new InsufficientFundsException("Not enough money"))
                .when(transferService).transfer(any(TransferRequest.class));

        String json = transferJacksonTester.write(transferRequest).getJson();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(transferService).transfer(any(TransferRequest.class));
    }

    @Test
    @WithMockUser
    void transfer_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        doThrow(new AccessDeniedException("Cards do not belong to this user"))
                .when(transferService).transfer(any(TransferRequest.class));

        String json = transferJacksonTester.write(transferRequest).getJson();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(transferService).transfer(any(TransferRequest.class));
    }

    @Test
    @WithMockUser
    void transfer_shouldReturnNotFound_whenCardIsAbsent() throws Exception {
        doThrow(new EntityNotFoundException())
                .when(transferService).transfer(any(TransferRequest.class));

        String json = transferJacksonTester.write(transferRequest).getJson();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        verify(transferService).transfer(any(TransferRequest.class));
    }

    @Test
    @WithMockUser
    void transfer_shouldReturnBadRequest_whenCardInactive() throws Exception {
        doThrow(new CardInactiveException("Card inactive"))
                .when(transferService).transfer(any(TransferRequest.class));

        String json = transferJacksonTester.write(transferRequest).getJson();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(transferService).transfer(any(TransferRequest.class));
    }
}