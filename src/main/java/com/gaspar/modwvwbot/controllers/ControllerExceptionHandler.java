package com.gaspar.modwvwbot.controllers;

import com.gaspar.modwvwbot.controllers.dto.ApiError;
import com.gaspar.modwvwbot.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions from controllers.
 */
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorized(UnauthorizedException e) {
        return new ApiError(e.getMessage());
    }

}
