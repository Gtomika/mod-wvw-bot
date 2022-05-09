package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Model class for one player in a cleaned wvw JSON log. Only
 * contains the fields needed later (this is why it is "cleaned").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class Player {

    //simple top level fields
    private String account;
    private int group;
    private boolean hasCommanderTag;
    private String profession;
    private boolean friendlyNPC;
    private boolean notInSquad;
    private String guildID;
    private String[] weapons;

    //complex fields
    private ExtHealingStats extHealingStats;
    private Buff[] buffUptimes;
    private Defense[] defenses;
    private Dps[] dpsAll;
    //for some reason it's an array in another array
    private List<Dps[]> dpsTargets;
    private Stat[] statsAll;
    private Support[] support;

}
