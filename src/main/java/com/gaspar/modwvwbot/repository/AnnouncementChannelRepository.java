package com.gaspar.modwvwbot.repository;

import com.gaspar.modwvwbot.model.AnnouncementChannel;
import com.gaspar.modwvwbot.model.WatchedChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementChannelRepository extends JpaRepository<AnnouncementChannel, Long> {

    Optional<AnnouncementChannel> getByGuildIdAndChannelId(Long guildId, Long channelId);

    List<AnnouncementChannel> getByGuildId(Long guildId);

}
