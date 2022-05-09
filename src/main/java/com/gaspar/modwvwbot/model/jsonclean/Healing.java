package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Healing data of a player in the cleaned Wvw log.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class Healing {

    private int hps;
    private int healing;
    private int healingPowerHps;
    private int healingPowerHealing;
    private int conversionHps;
    private int conversionHealing;
    private int hybridHps;
    private int hybridHealing;
    private int downedHps;
    private int downedHealing;

}
