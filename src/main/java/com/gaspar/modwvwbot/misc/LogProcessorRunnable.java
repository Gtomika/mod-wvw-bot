package com.gaspar.modwvwbot.misc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * Downloads and processes one attachment from a discord message.
 */
@Slf4j
@RequiredArgsConstructor
public class LogProcessorRunnable implements Runnable {

    /**
     * Discord message attachment to be downloaded and processed. It is already guaranteed
     * that it has a watched file extension.
     */
    private final Message.Attachment attachment;

    @Override
    public void run() {
        try {
            log.info("Processing of file '{}' has started.", attachment.getFileName());
            Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), attachment.getFileName());
            Files.createFile(tempFilePath);
            //download into temp file (can block, this is already on background thread
            attachment.downloadToFile(tempFilePath.toFile()).get();
            log.debug("File '{}' downloaded into '{}'", attachment.getFileName(), tempFilePath.toFile().getAbsolutePath());
            //TODO

            //delete temp file in the end
            Files.delete(tempFilePath);
        } catch (IOException e) {
            log.error("Failed to process file '{}' because of IO exception.", attachment.getFileName(), e);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Interrupted while waiting for the file to download.", e);
        }
    }
}
