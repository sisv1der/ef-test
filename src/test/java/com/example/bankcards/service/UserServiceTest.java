package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserInfoResponse userInfo;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private PageRequest pageable;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .username("username")
                .passwordHash("password")
                .build();

        userInfo = new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );

        createUserRequest = new CreateUserRequest("username", "password", Role.USER);

        updateUserRequest = new UpdateUserRequest("username1", "password", Role.ADMIN, false);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findMe_shouldReturn() {
        when(userRepository.findByUsername(any(String.class)))
                .thenReturn(Optional.of(user));
        when(authenticationService.getUsername())
                .thenReturn("username");

        UserInfoResponse result = userService.findMe();

        assertNotNull(result);
        assertEquals(userInfo, result);

        verify(userRepository).findByUsername(any(String.class));
    }

    @Test
    void findUser_shouldReturnUser_whenIdIsProvidedAndUserExists() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));

        UserInfoResponse result = userService.findUser(user.getId());

        assertNotNull(result);
        assertEquals(userInfo, result);

        verify(userRepository).findById(any(UUID.class));
    }

    @Test
    void findUser_shouldThrow_whenIdIsProvidedAndUserIsAbsent() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findUser(user.getId()));

        verify(userRepository).findById(any(UUID.class));
    }

    @Test
    void findUsers_shouldReturn_whenFieldsAreProvided() {
        when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<UserInfoResponse> result = userService.findUsers("username", Role.USER, true, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(userInfo, result.getContent().get(0));

        verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class));
    }

    @Test
    void findUsers_shouldReturn_whenFieldsAreNotProvided() {
        when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user, User.builder().username("username1").passwordHash("password1").build())));

        Page<UserInfoResponse> result = userService.findUsers("username", Role.USER, true, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(userInfo, result.getContent().get(0));

        verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class));
    }

    @Test
    void createUser_shouldCreate() {
        when(userRepository.existsByUsername(any(String.class)))
                .thenReturn(false);
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("password");

        UserInfoResponse result = userService.createUser(createUserRequest);

        assertNotNull(result);
        assertEquals(userInfo.username(), result.username());

        verify(userRepository).save(any(User.class));
        verify(userRepository).existsByUsername(any(String.class));
    }

    @Test
    void createUser_shouldThrow_whenAlreadyExists() {
        when(userRepository.existsByUsername(any(String.class)))
                .thenReturn(true);

        assertThrows(EntityExistsException.class, () -> userService.createUser(createUserRequest));

        verify(userRepository).existsByUsername(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldReturn() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("password");
        when(userRepository.existsByUsername(any(String.class)))
                .thenReturn(false);

        UserInfoResponse expected = new UserInfoResponse(
                user.getId(),
                "username1",
                Role.ADMIN,
                false,
                user.getCreatedAt()
        );

        UserInfoResponse result = userService.updateUser(updateUserRequest, user.getId());

        assertNotNull(result);
        assertEquals(expected, result);

        verify(userRepository).existsByUsername(any(String.class));
        verify(userRepository).findById(any(UUID.class));
    }

    @Test
    void updateUser_shouldThrow_whenUsernameExists() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(any(String.class)))
                .thenReturn(true);

        assertThrows(EntityExistsException.class, () -> userService.updateUser(updateUserRequest, user.getId()));

        verify(userRepository).findById(any(UUID.class));
        verify(userRepository).existsByUsername(any(String.class));
    }

    @Test
    void updateUser_shouldThrow_whenUserIsAbsent() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(updateUserRequest, user.getId()));

        verify(userRepository).findById(any(UUID.class));
        verify(userRepository, never()).existsByUsername(any(String.class));
    }

    @Test
    void deleteUser_shouldDelete() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));
        when(authenticationService.isMe(any(String.class)))
                .thenReturn(false);

        userService.deleteUser(user.getId());

        verify(userRepository).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldThrow_whenUserIsAbsent() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(user.getId()));

        verify(userRepository).findById(any(UUID.class));
        verify(authenticationService, never()).isMe(any(String.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldThrow_whenDeletingSelf() {
        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(user));
        when(authenticationService.isMe(any(String.class)))
                .thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(user.getId()));

        verify(userRepository).findById(any(UUID.class));
        verify(authenticationService).isMe(any(String.class));
        verify(userRepository, never()).delete(any(User.class));
    }
}