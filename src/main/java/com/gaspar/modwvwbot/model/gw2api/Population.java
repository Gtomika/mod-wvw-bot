package com.gaspar.modwvwbot.model.gw2api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GW2 world population.
 */
@AllArgsConstructor
public enum Population {

    Low("Alacsony"),
    Medium("Közepes"),
    High("Magas"),
    VeryHigh("Nagyon magas"),
    Full("Teljesen tele van");

    @Getter
    private final String hungarian;

}
