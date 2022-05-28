package com.gaspar.modwvwbot.controllers;

import com.gaspar.modwvwbot.controllers.dto.ApiError;
import com.gaspar.modwvwbot.exception.NotFoundException;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions from controllers.
 */
@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorized(UnauthorizedException e) {
        log.info("Unauthorized access to the bot API, message: '{}'", e.getMessage());
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.info("Resource was not found while accessing the bot API, message: '{}'", e.getMessage());
        return new ApiError(e.getMessage());
    }

}
