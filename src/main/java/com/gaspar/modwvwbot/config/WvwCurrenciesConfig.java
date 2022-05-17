package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * The currencies that are "wvw related" and queried when the /wvw_currencies
 * command is detected can be configured in this class.
 * @see com.gaspar.modwvwbot.model.WvwItemOrCurrency
 */
@Configuration
public class WvwCurrenciesConfig {

    /**
     * Add currencies here that will be returned by /wvw_currencies command.
     * This is probably better placed in the application.yml, but it's a
     * lot simpler this way.
     */
    private static final List<WvwItemOrCurrency> wvwCurrencies = List.of(
            new WvwItemOrCurrency(15, "Badges of Honor", "badge_of_honor", 971664799131918356L),
            new WvwItemOrCurrency(26, "Skirmish Claim Tickets", "skirmish_claim_ticket", 971665489904431105L),
            new WvwItemOrCurrency(31, "Proofs of Heroics", "heroics", 971666141225300048L),
            new WvwItemOrCurrency(36, "Proofs of Desert Heroics", "desert_heroics", 971666140877185037L),
            new WvwItemOrCurrency(65, "Proofs of Jade Heroics", "jade_heroics", 976053286975193118L)
    );

    @Bean
    @Qualifier("wvwCurrencies")
    public List<WvwItemOrCurrency> provideWvwCurrencies() {
        return wvwCurrencies;
    }

}
