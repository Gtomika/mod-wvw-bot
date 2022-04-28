package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.WvwRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WvwRoleRepository extends JpaRepository<WvwRole, Long> {

    Optional<WvwRole> findByGuildIdAndRoleId(Long guildId, Long roleId);

    List<WvwRole> findByGuildId(Long guildId);

}
