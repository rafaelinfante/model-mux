package net.rafaelinfante.modelmux.web.error;

import net.rafaelinfante.modelmux.gateway.AllProvidersFailedException;
import net.rafaelinfante.modelmux.provider.ProviderException;
import net.rafaelinfante.modelmux.provider.UnknownProviderException;
import net.rafaelinfante.modelmux.token.BudgetExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the gateway's domain failures to RFC 9457 problem responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BudgetExceededException.class)
    ResponseEntity<ProblemDetail> handleBudget(BudgetExceededException e) {
        ProblemDetail problem = problem(HttpStatus.TOO_MANY_REQUESTS, "Budget exceeded", e.getMessage());
        problem.setProperty("limitUsd", e.getLimitUsd());
        problem.setProperty("spentUsd", e.getSpentUsd());
        return response(problem);
    }

    @ExceptionHandler(UnknownProviderException.class)
    ResponseEntity<ProblemDetail> handleUnknownProvider(UnknownProviderException e) {
        return response(problem(HttpStatus.NOT_FOUND, "Unknown provider", e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException e) {
        return response(problem(HttpStatus.BAD_REQUEST, "Invalid request", e.getMessage()));
    }

    @ExceptionHandler(AllProvidersFailedException.class)
    ResponseEntity<ProblemDetail> handleAllFailed(AllProvidersFailedException e) {
        log.warn("All providers failed", e);
        return response(
                problem(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "No provider could serve the request",
                        e.getMessage()));
    }

    @ExceptionHandler(ProviderException.class)
    ResponseEntity<ProblemDetail> handleProvider(ProviderException e) {
        log.warn("Provider rejected the request", e);
        return response(problem(HttpStatus.BAD_GATEWAY, "Provider error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception e) {
        log.error("Unhandled error", e);
        return response(
                problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "An unexpected error occurred."));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }

    private static ResponseEntity<ProblemDetail> response(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }
}
