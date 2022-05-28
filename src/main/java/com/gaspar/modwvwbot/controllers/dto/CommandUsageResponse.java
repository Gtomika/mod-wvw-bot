package com.gaspar.modwvwbot.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommandUsageResponse {

    private String name;

    private int count;

    public void increaseWith(int count) {
        this.count += count;
    }

}
