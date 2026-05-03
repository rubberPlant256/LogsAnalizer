package org.strongcat.service.source;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.strongcat.data.LogEntry;
import org.strongcat.service.OpenSearchService;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class FileLogListener extends TailerListenerAdapter {

    private final OpenSearchService openSearchService;
    private final ObjectMapper objectMapper;
    private final String filePath;

    @Override
    public void handle(String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        try {
            LogEntry entry = objectMapper.readValue(line, LogEntry.class);
            openSearchService.saveLog(entry);
        } catch (Exception e) {
            log.error("Failed to process line from file: {}", line, e);
        }
    }

    @Override
    public void fileNotFound() {
        log.warn("Log file not found: {}", filePath);
    }

    @Override
    public void handle(Exception ex) {
        log.error("Error while tailing file: {}", filePath, ex);
    }
}