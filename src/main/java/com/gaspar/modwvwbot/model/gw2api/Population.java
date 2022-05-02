package com.gaspar.modwvwbot.model.gw2api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GW2 world population.
 */
@AllArgsConstructor
public enum Population {

    Low("Alacsony", 500),
    Medium("Közepes", 500),
    High("Magas", 1000),
    VeryHigh("Nagyon magas", 1800),
    Full("Teljesen tele van", Integer.MAX_VALUE);

    @Getter
    private final String hungarian;

    @Getter
    private final int transferCost;

}
