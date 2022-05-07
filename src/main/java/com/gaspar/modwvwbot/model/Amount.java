package com.gaspar.modwvwbot.model;

import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Stores an amount of a Wvw item of currency.
 * @see com.gaspar.modwvwbot.model.WvwItemOrCurrency
 */
@Data
@AllArgsConstructor
public class Amount {

    /**
     * The item that is counted.
     */
    private WvwItemOrCurrency itemOrCurrency;

    /**
     * How many {@link #itemOrCurrency} are counted.
     */
    private int amount;

    public void increaseAmountBy(int extra) {
        amount += extra;
    }
}
