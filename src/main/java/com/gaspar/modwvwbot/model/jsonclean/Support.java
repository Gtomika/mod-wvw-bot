package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class Support {

    private int resurrects;
    private int condiCleanse;
    private int condiCleanseSelf;
    private int boonStrips;

}
