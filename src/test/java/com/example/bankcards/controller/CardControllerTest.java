package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfiguration;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardStatusChangeException;
import com.example.bankcards.exception.EncryptionServiceException;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import({TestSecurityConfiguration.class, GlobalExceptionHandler.class})
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    private JacksonTester<UpdateCardStatusRequest> updateJacksonTester;
    private JacksonTester<CreateCardRequest> createJacksonTester;

    @MockitoBean(name = "cardService")
    private CardService cardService;
    @MockitoBean
    private JwtService jwtService;

    private CardInfoResponse cardInfoResponse;
    private UpdateCardStatusRequest updateCardStatusRequest;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    public void setUp() {
        JacksonTester.initFields(this, jsonMapper);

        UUID cardId = UUID.randomUUID();
        CardStatus cardStatus = CardStatus.ACTIVE;
        String ownerName = "ownerName";
        UUID ownerId = UUID.randomUUID();
        YearMonth expiryMonth = YearMonth.of(2029, 1);
        BigDecimal balance = new BigDecimal("100");

        cardInfoResponse = new CardInfoResponse(
                cardId,
                "decryptedCardNumber",
                cardStatus,
                ownerName,
                ownerId,
                expiryMonth,
                balance
        );

        updateCardStatusRequest = new UpdateCardStatusRequest(cardStatus);

        createCardRequest = new CreateCardRequest(UUID.randomUUID(), "OWNER_NAME");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getCard_shouldReturnOk_whenIsAdmin() throws Exception {
        when(cardService.getCard(cardInfoResponse.cardId()))
                .thenReturn(cardInfoResponse);

        mockMvc.perform(get("/api/cards/{id}", cardInfoResponse.cardId()))
                .andExpect(status().isOk());

        verify(cardService).getCard(cardInfoResponse.cardId());
    }

    @Test
    @WithMockUser(username = "user")
    void getCard_shouldReturnOk_whenIsNotAdminAndIsOwner() throws Exception {
        when(cardService.getCard(eq(cardInfoResponse.cardId())))
                .thenReturn(cardInfoResponse);
        when(cardService.isOwner(any(UUID.class), eq("user")))
                .thenReturn(true);

        mockMvc.perform(get("/api/cards/{id}", cardInfoResponse.cardId()))
                .andExpect(status().isOk());

        verify(cardService).getCard(cardInfoResponse.cardId());
        verify(cardService).isOwner(cardInfoResponse.cardId(), "user");
    }

    @Test
    @WithMockUser(username = "user")
    void getCard_shouldReturnForbidden_whenIsNotAdminAndIsNotOwner() throws Exception {
        when(cardService.isOwner(any(UUID.class), eq("user")))
                .thenReturn(false);

        mockMvc.perform(get("/api/cards/{id}", cardInfoResponse.cardId()))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(cardService, never()).getCard(cardInfoResponse.cardId());
        verify(cardService).isOwner(cardInfoResponse.cardId(), "user");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getCard_shouldReturnNotFound_whenCardDoesntExist() throws Exception {
        when(cardService.getCard(any()))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/api/cards/{id}", cardInfoResponse.cardId()))
                .andExpect(status().isNotFound());

        verify(cardService).getCard(cardInfoResponse.cardId());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getCard_shouldReturnInternalServerError_whenDecryptionFails() throws Exception {
        when(cardService.getCard(any()))
                .thenThrow(EncryptionServiceException.class);

        mockMvc.perform(get("/api/cards/{id}", cardInfoResponse.cardId()))
                .andExpect(status().isInternalServerError());

        verify(cardService).getCard(cardInfoResponse.cardId());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getCards_shouldReturnCards_whenIsAdmin() throws Exception {
        Page<CardInfoResponse> page = new PageImpl<>(List.of(cardInfoResponse), PageRequest.of(0, 10), 5);
        when(cardService.getCards(any(Pageable.class), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/cards")
                .param("page", "0")
                .param("size", "10")
                .param("status", "ACTIVE")
                .param("username", "username"))
                .andExpect(status().isOk());

        verify(cardService).getCards(any(Pageable.class), eq(CardStatus.ACTIVE), eq("username"));
    }

    @Test
    @WithMockUser
    void getCards_shouldReturnCards_whenIsUser() throws Exception {
        Page<CardInfoResponse> page = new PageImpl<>(List.of(cardInfoResponse), PageRequest.of(0, 10), 5);
        when(cardService.getCards(any(Pageable.class), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE")
                        .param("username", "username"))
                .andExpect(status().isOk());

        verify(cardService).getCards(any(Pageable.class), eq(CardStatus.ACTIVE), eq("username"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateCardStatus_shouldReturnOk_whenIsAdmin() throws Exception {
        when(cardService.updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class)))
                .thenReturn(cardInfoResponse);

        String json = updateJacksonTester.write(updateCardStatusRequest).getJson();

        mockMvc.perform(patch("/api/cards/{id}", cardInfoResponse.cardId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(cardService).updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class));
    }

    @Test
    @WithMockUser(username = "user")
    void updateCardStatus_shouldReturnOk_whenIsNotAdminAndIsOwner() throws Exception {
        when(cardService.isOwner(any(UUID.class), eq("user")))
                .thenReturn(true);
        when(cardService.updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class)))
                .thenReturn(cardInfoResponse);

        String json = updateJacksonTester.write(updateCardStatusRequest).getJson();

        mockMvc.perform(patch("/api/cards/{id}", cardInfoResponse.cardId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(cardService).updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class));
        verify(cardService).isOwner(any(UUID.class), eq("user"));
    }

    @Test
    @WithMockUser(username = "user")
    void updateCardStatus_shouldReturnForbidden_whenIsNotAdminAndIsNotOwner() throws Exception {
        when(cardService.isOwner(any(UUID.class), eq("user")))
                .thenReturn(false);

        String json = updateJacksonTester.write(updateCardStatusRequest).getJson();

        mockMvc.perform(patch("/api/cards/{id}", cardInfoResponse.cardId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(cardService, never()).updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class));
        verify(cardService).isOwner(any(UUID.class), eq("user"));
    }

    @Test
    @WithMockUser(username = "user")
    void updateCardStatus_shouldReturnNotFound_whenCardIsAbsent() throws Exception {
        when(cardService.isOwner(any(UUID.class), eq("user")))
                .thenReturn(true);
        when(cardService.updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class)))
                .thenThrow(EntityNotFoundException.class);

        String json = updateJacksonTester.write(updateCardStatusRequest).getJson();

        mockMvc.perform(patch("/api/cards/{id}", cardInfoResponse.cardId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        verify(cardService).updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class));
        verify(cardService).isOwner(any(UUID.class), eq("user"));
    }

    @Test
    @WithMockUser(username = "user")
    void updateCardStatus_shouldReturnBadRequest_whenCardStatusChangeFailed() throws Exception {
        when(cardService.isOwner(any(UUID.class), eq("user")))
                .thenReturn(true);
        when(cardService.updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class)))
                .thenThrow(CardStatusChangeException.class);

        String json = updateJacksonTester.write(updateCardStatusRequest).getJson();

        mockMvc.perform(patch("/api/cards/{id}", cardInfoResponse.cardId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(cardService).updateCardStatus(any(UUID.class), any(UpdateCardStatusRequest.class));
        verify(cardService).isOwner(any(UUID.class), eq("user"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCard_shouldReturnCreated_whenIsAdmin() throws Exception {
        when(cardService.createCard(any(CreateCardRequest.class)))
                .thenReturn(cardInfoResponse);

        String json = createJacksonTester.write(createCardRequest).getJson();

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/cards/" + cardInfoResponse.cardId())));

        verify(cardService).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCard_shouldReturnBadRequest_whenOwnerNameIsBlank() throws Exception {
        CreateCardRequest createCardRequest = new CreateCardRequest(UUID.randomUUID(), "");

        String json = createJacksonTester.write(createCardRequest).getJson();

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCard_shouldReturnNotFound_whenUserIsAbsent() throws Exception {
        when(cardService.createCard(any(CreateCardRequest.class)))
                .thenThrow(EntityNotFoundException.class);

        String json = createJacksonTester.write(createCardRequest).getJson();

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        verify(cardService).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser
    void createCard_shouldReturnForbidden_whenIsNotAdmin() throws Exception {
        String json = createJacksonTester.write(createCardRequest).getJson();

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(cardService, never()).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCard_shouldReturnNoContent_whenIsAdmin() throws Exception {
        doNothing().when(cardService).deleteCard(any(UUID.class));

        mockMvc.perform(delete("/api/cards/" + cardInfoResponse.cardId()))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCard_shouldReturnNotFound_whenCardIsAbsent() throws Exception {
        doThrow(EntityNotFoundException.class).when(cardService).deleteCard(any(UUID.class));

        mockMvc.perform(delete("/api/cards/" + cardInfoResponse.cardId()))
                .andExpect(status().isNotFound());

        verify(cardService).deleteCard(any(UUID.class));
    }

    @Test
    @WithMockUser
    void deleteCard_shouldReturnForbidden_whenIsNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/cards/" + cardInfoResponse.cardId()))
                .andExpect(status().isForbidden());

        verify(cardService, never()).deleteCard(any(UUID.class));
    }
}