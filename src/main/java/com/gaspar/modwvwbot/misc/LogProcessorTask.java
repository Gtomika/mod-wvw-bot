package com.gaspar.modwvwbot.misc;

import com.gaspar.modwvwbot.exception.DpsReportApiException;
import com.gaspar.modwvwbot.model.LogProcessingResult;
import com.gaspar.modwvwbot.model.dpsreportapi.DpsReportResponse;
import com.gaspar.modwvwbot.services.dpsreportapi.DpsReportService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Downloads and processes one attachment from a discord message.
 * @see LogProcessingResult
 */
@Slf4j
@AllArgsConstructor
@Builder
public class LogProcessorTask implements Callable<LogProcessingResult> {

    /**
     * Discord message attachment to be downloaded and processed. It is already guaranteed
     * that it has a watched file extension.
     */
    private final Message.Attachment attachment;

    /**
     * Communicates with dps.report API.
     */
    private final DpsReportService dpsReportService;

    /**
     * Path to created JSON file. This is null at the beginning as it is generated later.
     */
    @Nullable
    private Path jsonPath;

    /**
     * Downloads and processes the file. Then, the result is written to a file and returned.
     * It is the responsibility of the caller to delete this file after uploading it to discord.
     * @return Path to the result file.
     */
    @Override
    public LogProcessingResult call() {
        Path tempFilePath = null;
        try {
            log.info("Processing of file '{}' has started.", attachment.getFileName());
            tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), attachment.getFileName());
            Files.deleteIfExists(tempFilePath);
            Files.createFile(tempFilePath);
            //download into temp file (can block, this is already on background thread
            attachment.downloadToFile(tempFilePath.toFile()).get();
            log.debug("File '{}' downloaded into '{}'", attachment.getFileName(), tempFilePath.toFile().getAbsolutePath());
            //upload log and get json from dps.report
            log.debug("Uploading file '{}' to dps.report, creating permalink and JSON.", attachment.getFileName());
            DpsReportResponse dpsReportResponse = dpsReportService.getLogJson(tempFilePath.toFile());
            byte[] jsonBytes = dpsReportResponse.getLogJson().getBytes(StandardCharsets.UTF_8);
            log.debug("File '{}' was uploaded to dps.report with permalink '{}'. JSON downloaded, it is '{}' bytes long.",
                    attachment.getFileName(), dpsReportResponse.getPermalink(), jsonBytes.length);
            //save JSON to a file
            jsonPath = Paths.get(System.getProperty("java.io.tmpdir"), fileNameWithoutExtension(attachment.getFileName()) + ".json");

            return LogProcessingResult.builder()
                    .success(true)
                    .originalFileName(attachment.getFileName())
                    .permalink(dpsReportResponse.getPermalink())
                    .pathToLogJson(jsonPath)
                    .build();
        } catch (IOException e) {
            log.error("Failed to process file '{}' because of IO exception.", attachment.getFileName(), e);
            return LogProcessingResult.builder()
                    .success(false)
                    .originalFileName(attachment.getFileName())
                    .build();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Interrupted while processing log file '{}'.", attachment.getFileName(), e);
            return LogProcessingResult.builder()
                    .success(false)
                    .originalFileName(attachment.getFileName())
                    .build();
        } catch (DpsReportApiException e) {
            log.error("Failed to process log file '{}' because dps.report API returned error.", attachment.getFileName(), e);
            return LogProcessingResult.builder()
                    .success(false)
                    .originalFileName(attachment.getFileName())
                    .build();
        }
        finally {
            //delete temp log file in the end (this is NOT the JSON!)
            if(tempFilePath != null) {
                try {
                    Files.delete(tempFilePath);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary log file.");
                }
            }
        }
    }

    private String fileNameWithoutExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    /**
     * Deletes the resulting JSON file. Call this AFTER it was already uploaded to discord.
     */
    public void deleteJsonFile() {
        if(jsonPath != null) {
            try {
                Files.deleteIfExists(jsonPath);
                log.debug("Deleted JSON file at: '{}'", jsonPath.toFile().getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to delete JSON at '{}'", jsonPath.toFile().getAbsolutePath());
            }
        }
    }
}
