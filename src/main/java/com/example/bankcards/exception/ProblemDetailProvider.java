package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public class ProblemDetailProvider {

    private ProblemDetailProvider() {}

    public static ProblemDetail getProblemDetail(
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
