package com.gaspar.modwvwbot.model;

import com.gaspar.modwvwbot.model.gw2api.Population;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "home_world")
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class HomeWorld {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * Guild id. Unique since 1 guild can have only one home world.
     */
    @Column(name = "guild_id", nullable = false, unique = true)
    private Long guildId;

    /**
     * World ID from the GW2 API.
     */
    @Column(name = "world_id", nullable = false)
    private Integer worldId;

    /**
     * Name of the world from the GW2 API.
     */
    @Column(name = "world_name", nullable = false)
    private String worldName;

    /**
     * Population of the home world.
     */
    @Column(name = "population", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Population population;
}
