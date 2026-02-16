package com.example.exchangerateservice.advice;

import com.example.exchangerateservice.dto.response.ErrorResponse;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        return new ErrorResponse(
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                e.getMessage(),
                Instant.now());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException e) {
        return new ErrorResponse(
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                e.getMessage(),
                Instant.now());
    }

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

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Validation failed");
        return new ErrorResponse(
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                message,
                Instant.now());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Validation failed");
        return new ErrorResponse(
                BAD_REQUEST.value(),
                BAD_REQUEST.getReasonPhrase(),
                message,
                Instant.now());
    }

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
