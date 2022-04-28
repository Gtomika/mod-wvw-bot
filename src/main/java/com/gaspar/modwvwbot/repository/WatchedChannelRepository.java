package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.WatchedChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchedChannelRepository extends JpaRepository<WatchedChannel, Long> {

    Optional<WatchedChannel> getByGuildIdAndChannelId(Long guildId, Long channelId);

    List<WatchedChannel> getByGuildId(Long guildId);

}
