package com.gaspar.modwvwbot.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Month;

@Data
@AllArgsConstructor
public class MonthlyCommandUsageResponse {

    private int year;

    private Month month;

    private int count;

    public void increaseWith(int count) {
        this.count += count;
    }

}
