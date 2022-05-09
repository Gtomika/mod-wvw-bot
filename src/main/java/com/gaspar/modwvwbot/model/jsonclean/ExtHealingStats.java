package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * All healing stats of the player in the cleaned Wvw log.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class ExtHealingStats {

    private Healing[] outgoingHealing;

    //for some reason it's an array in another array
    private List<Healing[]> outgoingHealingAllies;

}
