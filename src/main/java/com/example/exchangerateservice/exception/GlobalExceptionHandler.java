package com.example.exchangerateservice.exception;

import com.example.exchangerateservice.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Invalid currency code (e.g., "XYZ") or no rate available for a pair.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        return new ErrorResponse(
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                e.getMessage(),
                Instant.now());
    }

    /**
     * Missing required query parameter (e.g., ?from= without value).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException e) {
        return new ErrorResponse(
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                e.getMessage(),
                Instant.now());
    }

    /**
     * Type mismatch (e.g., amount=abc instead of a number).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = String.format("Invalid value '%s' for parameter '%s'", e.getValue(), e.getName());
        return new ErrorResponse(
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                message,
                Instant.now());
    }

    /**
     * All exchange rate providers failed.
     */
    @ExceptionHandler(ExchangeRateUnavailableException.class)
    @ResponseStatus(SERVICE_UNAVAILABLE)
    public ErrorResponse handleExchangeRateUnavailable(ExchangeRateUnavailableException e) {
        log.error("Exchange rate service unavailable: {}", e.getMessage());
        return new ErrorResponse(
                SERVICE_UNAVAILABLE.value(),
                SERVICE_UNAVAILABLE.getReasonPhrase(),
                e.getMessage(),
                Instant.now());
    }

    /**
     * Catch-all for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception e) {
        log.error("Unexpected error", e);
        return new ErrorResponse(
                INTERNAL_SERVER_ERROR.value(),
                INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred",
                Instant.now());
    }
}
