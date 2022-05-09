package com.gaspar.modwvwbot.model.jsonclean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class CleanedWvwLogTest {

    //only on my computer!
    private Path testLogsPath = Paths.get("C:\\Programozas\\Java\\Projects\\ModWvWBot\\test_logs");

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testDeserializeLog() throws IOException {
        Path testJsonPath = testLogsPath.resolve("20220504-205710_wvw_kill.json");
        CleanedWvwLog cleanedLog = objectMapper.readValue(testJsonPath.toFile(), CleanedWvwLog.class);
        //to file
        Path cleanedJsonPath = testLogsPath.resolve("20220504-205710_wvw_kill_cleaned.json");
        Files.createFile(cleanedJsonPath);
        objectMapper.writeValue(cleanedJsonPath.toFile(), cleanedLog);
    }

}