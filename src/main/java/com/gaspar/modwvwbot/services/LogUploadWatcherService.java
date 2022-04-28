package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.misc.LogProcessorRunnable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service that monitors the watched channels for file uploads with the correct extension.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogUploadWatcherService extends ListenerAdapter {

    @Value("${com.gaspar.modwvwbot.watched_file_extensions}")
    private List<String> watchedFileExtensions;

    private final ExecutorService logProcessingExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final WatchedChannelCommandService watchedChannelCommandService;

    @PostConstruct
    public void postConstruct() {
        log.info("Watching for the following file extensions: {}", watchedFileExtensions);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        var attachments = event.getMessage().getAttachments();
        if(event.isFromGuild() && watchedChannelCommandService.isWatchedChannel(event.getGuild().getIdLong(), event.getChannel())
                && hasAttachmentWithWatchedExtension(attachments)) {
            //this message is of interest
            log.info("Message received in guild '{}', watched channel '{}' with attachments of interest. Starting async processing...",
                    event.getGuild().getName(), event.getChannel().getName());
            processFilesAsync(event);
        }
    }

    /**
     * Checks if a message's attachments has any files with the watched extensions.
     * @param attachments Attachments of the message.
     * @return True if there is at least 1 watched file in the message.
     */
    private boolean hasAttachmentWithWatchedExtension(List<Message.Attachment> attachments) {
        for(var attachment: attachments) {
            if(watchedFileExtensions.contains(attachment.getFileExtension())) {
                return true;
            }
        }
        return false;
    }

    private void processFilesAsync(MessageReceivedEvent event) {
        int counter = 0;
        for(var attachment: event.getMessage().getAttachments()) {
            if(watchedFileExtensions.contains(attachment.getFileExtension())) {
                //this attachment is of interest
                counter++;
                logProcessingExecutorService.execute(new LogProcessorRunnable(attachment));
            }
        }
        //reply that processing is started
        event.getMessage().reply("Megkezdtem " + counter + " darab támogatott fájl feldolgozását. Ez eltarthat egy ideig...").queue();
    }
}
