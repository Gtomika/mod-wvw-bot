package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.misc.LogProcessorTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Service that monitors the watched channels for file uploads with the correct extension. Only the watched channels
 * are monitored for file uploads.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogUploadWatcherService extends ListenerAdapter {

    @Value("${com.gaspar.modwvwbot.watched_file_extensions}")
    private List<String> watchedFileExtensions;

    private final ChannelCommandsService watchedChannelCommandService;

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
            //reply that processing is started
            event.getMessage().reply("Log fájlokat észleltem ezen a figyelt csatornán." +
                    " Egyenlőre az ezeket feldolgozó funckió nem aktív.").queue();
            /*
            event.getMessage().reply("Megkezdtem a log fájlok feldolgozását. Ahogy haladok, frissítem ezt " +
                            "az üzenetet. Ez eltarthat egy ideig...")
                    .mentionRepliedUser(false)
                    .queue(reply -> { //when discord reply was sent we start the processing
                        processFilesAsync(event, reply);
                    });
             */
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

    private void processFilesAsync(MessageReceivedEvent event, Message reply) {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        var tasks = new ArrayList<LogProcessorTask>();
        for(var attachment: event.getMessage().getAttachments()) {
            if(watchedFileExtensions.contains(attachment.getFileExtension())) {
                //this attachment is of interest
                tasks.add(new LogProcessorTask(attachment));
            }
        }

        //submit all tasks
        var futures = new ArrayList<Future<Path>>();
        for(var task: tasks) {
            futures.add(executor.submit(task));
        }

        executor.shutdown();
        try {
            //wait for results
            boolean finished = executor.awaitTermination(10, TimeUnit.MINUTES);
            if(finished) {
                log.info("Processing of all log files finished, uploading results to Discord...");
                var paths = new ArrayList<Path>();
                for(var future: futures) {
                    paths.add(future.get());
                }
                uploadFilesToDiscord(reply, paths);
            } else {
                //timed out
                log.warn("Timed out while waiting for the tasks to finish.");
                reply.editMessage("Kifutottam az időből, nem sikerült a fájlok feldolgozása.").queue();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for the log processing tasks to finish.", e);
            reply.editMessage("Hiba miatt nem sikerült a fájlok feldolgozása.").queue();
        } catch (ExecutionException e) {
            log.error("Failed to process file.", e);
            reply.editMessage("Hiba miatt nem sikerült a fájlok feldolgozása.").queue();
        }
    }

    /**
     * Upload files to discord.
     * @param message Message which is to be edited with the files.
     * @param paths Paths of the files.
     */
    private void uploadFilesToDiscord(Message message, List<Path> paths) {
        message.editMessage("Befejeztem!").queue(); //TODO
    }
}
