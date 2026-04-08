package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserSpecification;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AuthenticationService authenticationService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserInfoResponse findMe() {
        String username = authenticationService.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new UserInfoResponse(user.getId(), user.getUsername(), user.getRole(), user.getActive(), user.getCreatedAt());
    }

    public UserInfoResponse findUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return new UserInfoResponse(user.getId(), user.getUsername(), user.getRole(), user.getActive(), user.getCreatedAt());
    }

    public Page<UserInfoResponse> findUsers(
            String username,
            Role role,
            Boolean active,
            Pageable pageable
    ) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasUsername(username))
                .and(UserSpecification.hasRole(role))
                .and(UserSpecification.hasActive(active));

        Page<User> users = userRepository.findAll(spec, pageable);

        return users.map(u -> new UserInfoResponse(u.getId(), u.getUsername(), u.getRole(), u.getActive(), u.getCreatedAt()));
    }

    @Transactional
    public UserInfoResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new EntityExistsException("Username already exists");
        }

        User user = User.builder()
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .username(request.username())
                .build();

        userRepository.save(user);

        return new UserInfoResponse(user.getId(), user.getUsername(), user.getRole(), user.getActive(), user.getCreatedAt());
    }

    @Transactional
    public UserInfoResponse updateUser(UpdateUserRequest request, UUID userId) {
        User original = findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!original.getUsername().equals(request.username())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new EntityExistsException("Username already exists");
            }
        }

        original.setUsername(request.username());
        original.setRole(request.role());
        original.setActive(request.isActive());
        original.setPasswordHash(passwordEncoder.encode(request.password()));

        return new UserInfoResponse(original.getId(), original.getUsername(), original.getRole(), original.getActive(), original.getCreatedAt());
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (authenticationService.isMe(user.getUsername())) {
            throw new AccessDeniedException("You are not allowed to delete this user");
        }

        userRepository.delete(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
}
