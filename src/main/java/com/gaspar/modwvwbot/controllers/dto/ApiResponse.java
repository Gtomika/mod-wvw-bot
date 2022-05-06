package com.gaspar.modwvwbot.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simple wrapper for a message.
 */
@Data
@AllArgsConstructor
public class ApiResponse {

    private String message;

}
