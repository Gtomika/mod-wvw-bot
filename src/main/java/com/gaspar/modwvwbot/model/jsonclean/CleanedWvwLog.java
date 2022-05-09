package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is the model class of the cleaned JSON log files, which contain
 * only the fields necessary for the Power BI data visualization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class CleanedWvwLog {

    //simple top level attributes
    private String eliteInsightsVersion;
    private int triggerID;
    private String fightName;
    private String fightIcon;
    private String arcVersion;
    @JsonProperty("gW2Build")
    private String gW2Build;
    private String recordedBy;
    private String timeStart;
    private String timeEnd;
    private String timeStartStd;
    private String timeEndStd;
    private String duration;
    private boolean success;
    @JsonProperty("isCM")
    private boolean isCM;
    //targets
    private Target[] targets;
    //players
    private Player[] players;
    //other relatively small attributes that are to be kept in full
    private JsonNode phases;
    private JsonNode mechanics;
    //private JsonNode buffMap;
    private JsonNode logErrors;
    private JsonNode usedExtensions;

}
