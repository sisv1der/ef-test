package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users API",
        description = "API для пользователей"
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получить текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<UserInfoResponse> getMe() {
        return ResponseEntity.ok(userService.findMe());
    }

    @Operation(summary = "Получить пользователя по ID (только ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> getUser(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userService.findUser(id));
    }

    @Operation(summary = "Получить список пользователей (только ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список пользователей получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserInfoResponse> getUsers(
            @Parameter(description = "Username пользователя", example = "john_doe123")
            @RequestParam (required = false) String username,

            @Parameter(description = "Роль пользователя", example = "ADMIN")
            @RequestParam (required = false) Role role,

            @Parameter(description = "Статус пользователя", example = "true")
            @RequestParam (required = false) Boolean active,
            @ParameterObject Pageable pageable
    ) {
        return userService.findUsers(username, role, active, pageable);
    }

    @Operation(summary = "Создать пользователя (только ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Запрос на создание пользователя",
                    content = @Content(schema = @Schema(implementation = CreateUserRequest.class)),
                    required = true
            )
            @RequestBody CreateUserRequest request
    ) {
        request.validate();

        UserInfoResponse response = userService.createUser(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Обновить пользователя (только ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "409", description = "Username уже занят",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> updateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Запрос на обновление данных пользователя",
                    content = @Content(schema = @Schema(implementation = UpdateUserRequest.class)),
                    required = true
            )
            @RequestBody UpdateUserRequest request,

            @Parameter(description = "Идентификатор пользователя", required = true)
            @PathVariable UUID id
    ) {
        request.validate();

        return ResponseEntity.ok(userService.updateUser(request, id));
    }

    @Operation(summary = "Удалить пользователя (только ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён или попытка удалить себя",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неавторизован",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "Идентификатор пользователя", required = true)
            @PathVariable UUID id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
