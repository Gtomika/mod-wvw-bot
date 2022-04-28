package com.gaspar.modwvwbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Table which stores guild id - role id pairs: which roles are to be mentioned by the bot
 * in which servers.
 */
@Entity
@Table(name = "wvw_role", uniqueConstraints = @UniqueConstraint(columnNames = {"guild_id","role_id"}))
@Getter
@Setter
@NoArgsConstructor
public class WvwRole {

    public WvwRole(Long guildId, Long roleId) {
        this.guildId = guildId;
        this.roleId = roleId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "guild_id")
    private Long guildId;

    @Column(name = "role_id")
    private Long roleId;

}
