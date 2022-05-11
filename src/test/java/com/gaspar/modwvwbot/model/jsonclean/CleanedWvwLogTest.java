package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaspar.modwvwbot.misc.WvwLogUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class CleanedWvwLogTest {

    //only on my computer!
    private Path testLogsPath = Paths.get("C:\\Programozas\\Java\\Projects\\ModWvWBot\\test_logs");

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCleanLog() throws IOException {
        Path testJsonPath = testLogsPath.resolve("20220504-205710_wvw_kill_pretty.json");

        String json = Files.readString(testJsonPath);
        var targets = WvwLogUtils.extractTargets(json);

        CleanedWvwLog cleanedLog = objectMapper.readValue(json, CleanedWvwLog.class);
        cleanedLog.setTargets(targets);
        //to file
        Path cleanedJsonPath = testLogsPath.resolve("20220504-205710_wvw_kill_cleaned.json");
        Files.deleteIfExists(cleanedJsonPath);
        Files.createFile(cleanedJsonPath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(cleanedJsonPath.toFile(), cleanedLog);
    }

}