package com.gaspar.modwvwbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a Wvw related item or currency that is queried in the
 * /wvw_items or /wvw_currencies command.
 * @see com.gaspar.modwvwbot.config.WvwItemsConfig
 * @see com.gaspar.modwvwbot.config.WvwCurrenciesConfig
 */
@Data
@AllArgsConstructor
public class WvwItemOrCurrency {

    /**
     * GW2 ID of this item/currency.
     */
    private int id;

    /**
     * Name if the item/currency.
     */
    private String name;

    /**
     * Name of the emote that should be shown next to this item/currency.
     */
    private String emoteName;

    /**
     * ID of the emote that should be shown next to this item/currency.
     */
    private long emoteId;

}
