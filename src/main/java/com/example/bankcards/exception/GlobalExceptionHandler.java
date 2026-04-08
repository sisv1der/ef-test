package com.example.bankcards.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import static com.example.bankcards.exception.ErrorCode.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtServiceException.class)
    public ProblemDetail handleJOSEException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "JWT processing failed",
                JWT_FAILED,
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Bad Credentials",
                BAD_CREDENTIALS,
                request
        );
    }

    @ExceptionHandler(EncryptionServiceException.class)
    public ProblemDetail handleEncryptionServiceException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Card encryption failed",
                CARD_ENCRYPTION_FAILED,
                request
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDeniedException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.FORBIDDEN,
                "Authorization failed",
                AUTHORIZATION_FAILED,
                request
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(WebRequest request, Throwable ex) {
        return getProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed: " + ex.getMessage(),
                BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(CardStatusChangeException.class)
    public ProblemDetail handleCardStatusChangeException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Card status change failed",
                BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(EntityExistsException.class)
    public ProblemDetail handleEntityExistsException(WebRequest request, Throwable ex) {
        return getProblemDetail(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                CONFLICT,
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.FORBIDDEN,
                "This operation is forbidden",
                FORBIDDEN,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(WebRequest request) {
        return getProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                INTERNAL_SERVER_ERROR,
                request
        );
    }

    private ProblemDetail getProblemDetail(
            HttpStatus status,
            String message,
            ErrorCode errorCode,
            WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        Instant timestamp = Instant.now();

        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setType(URI.create("/errors/" + errorCode.toString()));
        pd.setTitle(status.getReasonPhrase());
        pd.setDetail(Optional.ofNullable(message).orElse("UnexpectedError"));
        pd.setProperty("errorCode", errorCode);
        pd.setProperty("timestamp", timestamp);
        pd.setInstance(URI.create(path));

        return pd;
    }
}
