package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class for one target in a cleaned wvw JSON log. Only
 * contains the fields needed later (this is why it is "cleaned").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class Target {

    private String name;
    private boolean enemyPlayer;
    private int instanceId;

}
