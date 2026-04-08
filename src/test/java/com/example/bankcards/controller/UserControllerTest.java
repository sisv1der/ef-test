package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfiguration;
import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import jakarta.persistence.EntityExistsException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfiguration.class, GlobalExceptionHandler.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    private JacksonTester<UpdateUserRequest> updateJacksonTester;
    private JacksonTester<CreateUserRequest> createJacksonTester;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private UserInfoResponse userInfo;
    private UpdateUserRequest updateUserRequest;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        JacksonTester.initFields(this, jsonMapper);

        userInfo = new UserInfoResponse(UUID.randomUUID(), "username", Role.ADMIN, true, Instant.now());

        updateUserRequest = new UpdateUserRequest("username", "password", Role.USER, false);

        createUserRequest = new CreateUserRequest("username", "password", Role.ADMIN);
    }

    @Test
    @WithMockUser(username = "username")
    void getMe_shouldReturnOk() throws Exception {
        when(userService.findMe())
                .thenReturn(userInfo);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk());

        verify(userService).findMe();
    }

    @Test
    void getMe_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUser_shouldReturnOk_whenIsAdmin() throws Exception {
        when(userService.findUser(any(UUID.class)))
                .thenReturn(userInfo);

        mockMvc.perform(get("/api/users/{id}", userInfo.id()))
                .andExpect(status().isOk());

        verify(userService).findUser(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUser_shouldReturnNotFound_whenIsAdminAndUserIsAbsent() throws Exception {
        when(userService.findUser(any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/api/users/{id}", userInfo.id()))
                .andExpect(status().isNotFound());

        verify(userService).findUser(any(UUID.class));
    }

    @Test
    @WithMockUser
    void getUser_shouldReturnForbidden_whenIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/users/{id}", userInfo.id()))
                .andExpect(status().isForbidden());

        verify(userService, never()).findUser(any(UUID.class));
    }

    @Test
    void getUser_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{id}", userInfo.id()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUsers_shouldReturnOk() throws Exception {
        Page<UserInfoResponse> page = new PageImpl<>(List.of(userInfo));

        when(userService.findUsers(any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/users")
                        .param("username", userInfo.username())
                        .param("role", userInfo.role().name())
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(userService).findUsers(eq(userInfo.username()), eq(userInfo.role()), eq(true), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void getUsers_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("username", userInfo.username())
                        .param("role", userInfo.role().name())
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());

        verify(userService, never()).findUsers(any(String.class), any(Role.class), any(Boolean.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_shouldReturnAll_whenNoFilters() throws Exception {
        Page<UserInfoResponse> page = new PageImpl<>(List.of());

        when(userService.findUsers(any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(userService).findUsers(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getUsers_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_ShouldReturnCreated_whenIsAdminAndUserIsAbsent() throws Exception {
        when(userService.createUser(any())).thenReturn(userInfo);

        String json = createJacksonTester.write(createUserRequest).getJson();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/users/" + userInfo.id())));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_ShouldReturnConflict_whenIsAdminAndUserExists() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(EntityExistsException.class);

        String json = createJacksonTester.write(createUserRequest).getJson();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser
    void createUser_ShouldReturnForbidden_whenIsNotAdmin() throws Exception {
        String json = createJacksonTester.write(createUserRequest).getJson();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_ShouldReturnBadRequest_whenValidationFails() throws Exception {
        createUserRequest = new CreateUserRequest("ame", "pad", null);

        String json = createJacksonTester.write(createUserRequest).getJson();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_shouldReturnOk_whenIsAdminAndUserIsPresent() throws Exception {
        when(userService.updateUser(any(UpdateUserRequest.class), any(UUID.class)))
                .thenReturn(userInfo);

        String json = updateJacksonTester.write(updateUserRequest).getJson();

        mockMvc.perform(put("/api/users/{id}", userInfo.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userService).updateUser(any(UpdateUserRequest.class), any(UUID.class));
    }

    @Test
    void  updateUser_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(put("/api/users/{id}", userInfo.id()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void updateUser_shouldReturnForbidden_whenIsNotAdmin() throws Exception {
        String json = updateJacksonTester.write(updateUserRequest).getJson();

        mockMvc.perform(put("/api/users/{id}", userInfo.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_shouldReturnBadRequest_whenValidationFails() throws Exception {
        updateUserRequest = new UpdateUserRequest("asd", "asd", null, null);

        String json = updateJacksonTester.write(updateUserRequest).getJson();

        mockMvc.perform(put("/api/users/{id}", userInfo.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_shouldReturnNotFound_whenUserIsAbsent() throws Exception {
        when(userService.updateUser(any(UpdateUserRequest.class), any(UUID.class)))
                .thenThrow(EntityNotFoundException.class);

        String json = updateJacksonTester.write(updateUserRequest).getJson();

        mockMvc.perform(put("/api/users/{id}", userInfo.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(any(UpdateUserRequest.class), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_shouldReturnConflict_whenUsernameTaken() throws Exception {
        when(userService.updateUser(any(UpdateUserRequest.class), any(UUID.class)))
                .thenThrow(EntityExistsException.class);

        String json = updateJacksonTester.write(updateUserRequest).getJson();

        mockMvc.perform(put("/api/users/{id}", userInfo.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());

        verify(userService).updateUser(any(UpdateUserRequest.class), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnNoContent_whenIsAdmin() throws Exception {
        doNothing().when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/api/users/{id}", userInfo.id()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(any(UUID.class));
    }

    @Test
    @WithMockUser
    void deleteUser_shouldReturnForbidden_whenIsNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userInfo.id()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnNotFound_whenUserIsAbsent() throws Exception {
        doThrow(EntityNotFoundException.class).when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/api/users/{id}", userInfo.id()))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnForbidden_whenDeletingSelf() throws Exception {
        doThrow(AccessDeniedException.class).when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/api/users/{id}", userInfo.id()))
                .andExpect(status().isForbidden());

        verify(userService).deleteUser(any(UUID.class));
    }

    @Test
    void deleteUser_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userInfo.id()))
                .andExpect(status().isUnauthorized());
    }
}