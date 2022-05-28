package com.gaspar.modwvwbot.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response body of guilds endpoint.
 */
@Data
public class GuildsResponse {

    private List<GuildResponse> guilds;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GuildResponse {
        private String guildName;
        private Long guildId;
    }

}
