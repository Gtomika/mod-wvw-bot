package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.HomeWorld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeWorldRepository extends JpaRepository<HomeWorld, Long> {

    Optional<HomeWorld> findByGuildId(long guildId);

}
