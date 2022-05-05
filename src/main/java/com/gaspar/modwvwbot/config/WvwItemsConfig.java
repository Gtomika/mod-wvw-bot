package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * The items that are "wvw related" and queried when the /wvw_items
 * command is detected can be configured in this class.
 * @see com.gaspar.modwvwbot.model.WvwItemOrCurrency
 */
@Configuration
public class WvwItemsConfig {

    /**
     * Add items here that are going to be returned by /wvw_items command.
     * This is probably better placed in the application.yml, but it's a
     * lot simpler this way.
     */
    private static final List<WvwItemOrCurrency> wvwItems = List.of(
            new WvwItemOrCurrency(71581, "Memories of battle", "memory_of_battle", 971658832554098698L),
            new WvwItemOrCurrency(93146, "Emblems of the Conqueror", "emblem_conqueror", 971661200377126975L),
            new WvwItemOrCurrency(93075, "Emblems of the Avenger", "emblem_avenger", 971661200377126975L),
            new WvwItemOrCurrency(81296, "Legendary Spikes", "legendary_spike", 971662031176491029L),
            new WvwItemOrCurrency(19678, "Gifts of Battle", "gift_of_battle", 971662430461657159L)
    );

    @Bean
    @Qualifier("wvwItems")
    public List<WvwItemOrCurrency> provideWvwItems() {
        return wvwItems;
    }

}
