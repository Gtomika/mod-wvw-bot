package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.misc.EmoteUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

/**
 * Service which manages which users are able to invoke the bot commands.
 *  - Server admins are able to use the commands any time.
 *  - Users with {@link com.gaspar.modwvwbot.model.ManagerRole}s are able to use the commands.
 */
@Service
@Slf4j
public class AuthorizationService {

    @Value("${com.gaspar.modwvwbot.emote_ids.commander}")
    private long commanderEmoteId;

    @Value("${com.gaspar.modwvwbot.reject_all_authorized_commands}")
    private boolean rejectAllAuthorizedCommands;

    @Value("${com.gaspar.modwvwbot.security_token}")
    private String securityToken;

    private final RoleCommandsService roleCommandsService;

    //Lombok won't copy annotation, so this constructor is needed.
    //must use @Lazy, there is a circular dependency.
    @Autowired
    public AuthorizationService(@Lazy RoleCommandsService roleCommandsService) {
        this.roleCommandsService = roleCommandsService;

    }

    @PostConstruct
    public void init() {
        if(rejectAllAuthorizedCommands) {
            log.warn("Rejecting all authorized commands! This is a debug property.");
        }
    }

    /**
     * Test if a member is authorized to invoke bot commands.
     */
    public boolean isUnauthorizedToManageBot(@Nullable Member member) {
        if(rejectAllAuthorizedCommands) return true;
        if(member == null) return true;
        if(member.getPermissions().contains(Permission.ADMINISTRATOR)) {
            return false; //admins can always manage
        }
        var managerRoleIds = roleCommandsService.getManagerRoleIds(member.getGuild().getIdLong());
        for(long managerRoleId: managerRoleIds) {
            if(member.getRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toList()).contains(managerRoleId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Decides if a token is valid to call API endpoints.
     * @param token The token.
     * @return True if invalid, false otherwise.
     */
    public boolean isUnauthorizedToCallApi(String token) {
        if(token == null) return true;
        return !securityToken.equals(token);
    }

    /**
     * Authorizes a slash command. If unauthorized, a response is sent.
     * @param event Event with details. DO NOT use this to reply!
     * @param hook Interaction hook, use to reply.
     * @return True if authorized. If false, then response was sent to the command already.
     */
    public boolean authorize(SlashCommandInteractionEvent event, InteractionHook hook) {
        if(isUnauthorizedToManageBot(event.getMember())) {
            log.info("Unauthorized user '{}' attempted to invoke command '{}'", event.getUser().getName(), event.getCommandString());
            hook.editOriginal(getUnauthorizedMessage()).queue();
            return false;
        }
        return true;
    }

    public String getUnauthorizedMessage() {
        String commander = EmoteUtils.customEmote("commander", commanderEmoteId);
        return "Hopp??, ehhez nincs jogosults??god! Nekem csak a commander " + commander + " parancsolhat.";
    }
}
