package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
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
