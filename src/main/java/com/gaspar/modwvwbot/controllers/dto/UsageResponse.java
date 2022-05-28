package com.gaspar.modwvwbot.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Generic usage response with only one top level field: "usage". Contains a
 * list of usage statistics.
 * @param <T> Statistics type.
 */
@Data
@AllArgsConstructor
public class UsageResponse<T> {

    private List<T> usage;

}
