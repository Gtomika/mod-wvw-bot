package com.gaspar.modwvwbot.services.botapi;

import com.gaspar.modwvwbot.controllers.dto.AnnouncementRequest;
import com.gaspar.modwvwbot.controllers.dto.AnnouncementResponse;
import com.gaspar.modwvwbot.model.AnnouncementChannel;
import com.gaspar.modwvwbot.repository.AnnouncementChannelRepository;
import com.gaspar.modwvwbot.services.RoleCommandsService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Service which posts announcements to the announcement channels of guilds.
 */
@Service
@Slf4j
public class AnnouncementService {

    private final JDA jda;
    private final AnnouncementChannelRepository announcementChannelRepository;
    private final RoleCommandsService roleCommandsService;

    public AnnouncementService(@Lazy JDA jda, AnnouncementChannelRepository announcementChannelRepository, RoleCommandsService roleCommandsService) {
        this.jda = jda;
        this.announcementChannelRepository = announcementChannelRepository;
        this.roleCommandsService = roleCommandsService;
    }

    /**
     * Make announcements in guilds.
     * @param request Request with details.
     * @return Response with details.
     */
    public AnnouncementResponse publishAnnouncements(AnnouncementRequest request) {
        log.info("Posting announcement according to request: {}", request);

        int postChannelCount = 0, failChannelCount = 0;
        Set<Long> postedGuildIds = new HashSet<>();
        for(AnnouncementChannel channel: announcementChannelRepository.findAll()) {
            if(request.getGuildIds() == null || request.getGuildIds().contains(channel.getGuildId())) {
                //post on this channel
                TextChannel textChannel = jda.getTextChannelById(channel.getChannelId());
                if(textChannel != null && textChannel.canTalk()) {
                    postChannelCount++;
                    String message = getMessageWithRoles(request, channel.getGuildId());
                    textChannel.sendMessage(message).queue();
                } else {
                    failChannelCount++;
                }
                postedGuildIds.add(channel.getChannelId());
            }
        }

        var response = new AnnouncementResponse(
                "Announcement successfully posted",
                postChannelCount,
                failChannelCount,
                postedGuildIds.size());
        log.info("Announcements posted, result is: {}", response);
        return response;
    }

    /**
     * Format announcement message to include the roles to be mentioned.
     */
    private String getMessageWithRoles(AnnouncementRequest request, long guildId) {
        var message = new StringBuilder();
        message.append(request.getMessage());
        if(request.getMentionWvwRoles()) {
            message.append("\n");
            for(String role: roleCommandsService.getWvwRoleIdsFormatted(guildId)) {
                message.append(role).append(" ");
            }
        }
        if(request.getMentionManagerRoles()) {
            message.append("\n");
            for(String role: roleCommandsService.getManagerRoleIdsFormatted(guildId)) {
                message.append(role).append(" ");
            }
        }
        return message.toString();
    }

}
