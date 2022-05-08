package com.gaspar.modwvwbot.services;

import com.gaspar.modwvwbot.misc.EmoteUtils;
import com.gaspar.modwvwbot.misc.LogProcessorTask;
import com.gaspar.modwvwbot.misc.TimeUtils;
import com.gaspar.modwvwbot.model.LogProcessingResult;
import com.gaspar.modwvwbot.services.dpsreportapi.DpsReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    @Value("${com.gaspar.modwvwbot.emote_ids.loading}")
    private long loadingId;

    private final ChannelCommandsService watchedChannelCommandService;
    private final DpsReportService dpsReportService;

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
            if(event.getTextChannel().canTalk()) {
                //reply that processing is started
                String loading = EmoteUtils.animatedEmote("loading", loadingId);
                event.getMessage().reply("Megkezdtem a log fájlok feldolgozását. Ez eltarthat egy ideig..." + loading + "\n" +
                                "Amint végeztem küldök egy új üzenetet az eredménnyel és megpingellek.")
                        .mentionRepliedUser(false)
                        .queue(reply -> { //when discord reply was sent we start the processing
                            processFilesAsync(event, reply);
                        });
            } else {
                log.warn("Can't talk on the watched channel '{}' where log files were uploaded. Ignoring...", event.getTextChannel().getName());
            }
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
        long startTime = System.currentTimeMillis();

        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        var tasks = new ArrayList<LogProcessorTask>();
        for(var attachment: event.getMessage().getAttachments()) {
            if(watchedFileExtensions.contains(attachment.getFileExtension())) {
                //this attachment is of interest, start task with it
                tasks.add(
                        LogProcessorTask.builder()
                                .attachment(attachment)
                                .dpsReportService(dpsReportService)
                        .build()
                );
            }
        }

        //submit all tasks
        var futures = new ArrayList<Future<LogProcessingResult>>();
        for(var task: tasks) {
            futures.add(executor.submit(task));
        }

        executor.shutdown();
        try {
            //wait for results
            boolean finished = executor.awaitTermination(10, TimeUnit.MINUTES);
            if(finished) {
                log.info("Processing of all log files finished, uploading results to Discord...");
                var results = new ArrayList<LogProcessingResult>();
                for(var future: futures) {
                    results.add(future.get());
                }
                //send results back to discord
                sendResponseToDiscord(
                        event.getAuthor().getIdLong(),
                        event.getTextChannel(),
                        reply,
                        results,
                        System.currentTimeMillis() - startTime
                );
                //delete JSONs
                for(var task: tasks) {
                    task.deleteJsonFile();
                }
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
     * @param userId Discord ID of the user who uploaded the files.
     * @param logChannel Channel on which the logs were posted.
     * @param reply Message which is showing progress to the user. Delete this and send a new one (because edits cannot ping).
     * @param results Results of {@link LogProcessorTask}s.
     * @param durationMillis Duration of processing in milliseconds.
     */
    private void sendResponseToDiscord(long userId, TextChannel logChannel, Message reply, List<LogProcessingResult> results, long durationMillis) {
        var message = new StringBuilder();
        String doneEmote = EmoteUtils.defaultEmote("white_check_mark");
        String userWhoUploaded = "<@" + userId + ">";
        message.append("Hé, ").append(userWhoUploaded).append("!\n");
        message.append("Végeztem ").append(results.size()).append(" log feldolgozásával ")
                .append(TimeUtils.createHungarianDurationStringFromSeconds(durationMillis/1000))
                .append(" alatt ").append(doneEmote).append("\n\n");
        for(var result: results) {
            message.append("*").append(result.getOriginalFileName()).append("* eredménye:\n");
            if(result.isSuccess()) {
                message.append(" - Dps Report permalink: <").append(result.getPermalink()).append(">\n");
                message.append(" - Tisztított JSON link: Nincs implementálva.\n");
            } else {
                String fail = EmoteUtils.defaultEmote("no_entry_sign");
                message.append(" - Hiba történt ").append(fail).append("\n");
            }
            message.append("\n");
        }
        //delete replay and send message with mention
        reply.delete().queue();
        if(logChannel.canTalk()) {
            logChannel.sendMessage(message.toString()).queue();
        } else {
            log.warn("Can't talk on channel '{}' where logs were uploaded. Failed to upload results.", logChannel.getName());
        }
    }
}
