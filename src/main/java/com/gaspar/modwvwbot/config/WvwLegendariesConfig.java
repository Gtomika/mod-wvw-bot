package com.gaspar.modwvwbot.config;

import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * configure which legendary items are Wvw related.
 */
@Configuration
public class WvwLegendariesConfig {

    /**
     * TODO: legendary WvW armors are not included, I can't find IDs
     */
    private static final List<WvwItemOrCurrency> wvwLegendaries = List.of(
            new WvwItemOrCurrency(81462, "Warbringer", "warbringer", 974202006921179176L),
            new WvwItemOrCurrency(93105, "Conflux", "conflux", 974202007097331732L)
    );

    @Bean
    @Qualifier("wvwLegendaries")
    public List<WvwItemOrCurrency> provideLegendaries() {
        return wvwLegendaries;
    }
}
