package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * General stats of a player in the cleaned Wvw log.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class Stat {

    private float stackDist;
    private float distToCom;
    private int swapCount;
    private int missed;
    private int evaded;
    private int blocked;
    private int interrupts;
    private int invulned;
    private int killed;
    private int downed;

}
