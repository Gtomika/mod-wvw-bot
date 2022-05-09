package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Players defensive stats in the cleaned Wvw log.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class Defense {

    private int damageTaken;
    private int breakbarDamageTaken;
    private int blockedCount;
    private int evadedCount;
    private int missedCount;
    private int dodgeCount;
    private int invulnedCount;
    private int damageBarrier;
    private int interruptedCount;
    private int downCount;
    private int downDuration;
    private int deadCount;
    private int deadDuration;
    private int dcCount;
    private int dcDuration;

}
