package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.ManagerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRoleRepository extends JpaRepository<ManagerRole, Long> {

    Optional<ManagerRole> findByGuildIdAndRoleId(Long guildId, Long roleId);

    List<ManagerRole> findByGuildId(Long guildId);

}
