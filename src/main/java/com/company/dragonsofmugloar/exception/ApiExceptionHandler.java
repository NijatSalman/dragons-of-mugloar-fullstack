package com.company.dragonsofmugloar.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every exception into an RFC 9457 {@link ProblemDetail} carrying the current traceId (put into the MDC by
 * Micrometer Tracing), so the frontend can show a message and the matching log lines can be found. Framework exceptions (validation, unknown path,
 * wrong method) are handled by the {@link ResponseEntityExceptionHandler} base class.
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TRACE_ID = "traceId";

    @ExceptionHandler(GameNotFoundException.class)
    ProblemDetail handleGameNotFound(GameNotFoundException exception) {
        log.debug("Game not found: gameId={}", exception.getGameId());
        return problemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AutoplayGameSessionNotFoundException.class)
    ProblemDetail handleRunNotFound(AutoplayGameSessionNotFoundException exception) {
        log.debug("Autoplay session not found: sessionId={}", exception.getSessionId());
        return problemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(GameOverException.class)
    ProblemDetail handleGameOver(GameOverException exception) {
        log.info("Game over: gameId={}", exception.getGameId());
        return problemDetail(HttpStatus.GONE, exception.getMessage());
    }

    @ExceptionHandler(AdNotAvailableException.class)
    ProblemDetail handleAdNotAvailable(AdNotAvailableException exception) {
        log.info("Ad not available: gameId={}, adId={}", exception.getGameId(), exception.getAdId());
        return problemDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(GameApiException.class)
    ProblemDetail handleGameApi(GameApiException exception) {
        log.warn("Game server unavailable: reason={}", exception.getMessage());
        return problemDetail(HttpStatus.BAD_GATEWAY, "Game server is currently unavailable");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        log.error("Unexpected error: type={}", exception.getClass().getSimpleName(), exception);
        return problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private static ProblemDetail problemDetail(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        String traceId = MDC.get(TRACE_ID);
        if (traceId != null) {
            problem.setProperty(TRACE_ID, traceId);
        }
        return problem;
    }
}
