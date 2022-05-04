package com.gaspar.modwvwbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * Table to store user id and api key pairs.
 */
@Entity
@Table(name = "api_keys", uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "api_key"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ApiKey {

    public ApiKey(Long userId, String key) {
        this.userId = userId;
        this.key = key;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * ID of user.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * API key of the user
     */
    @Column(name = "api_key")
    private String key;
}
