package org.strongcat.service.source;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.strongcat.service.OpenSearchService;
import org.strongcat.service.source.impl.LogSource;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.Duration;

import static org.strongcat.constant.FileSettings.READ_FILE_DURATION_MS;
import static org.strongcat.constant.SourceType.FILE_SOURCE;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.source.type", havingValue = FILE_SOURCE)
public class FileLogSource implements LogSource {

    private final OpenSearchService openSearchService;
    private final ObjectMapper objectMapper;

    @Value("${app.file.path}")
    private String filePath;

    private Tailer tailer;

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void startListening() {
        runTailerLoop();
    }

    @Async
    protected void runTailerLoop() {
        File file = new File(filePath);
        log.info("Starting background tailer for: {}", file.getAbsolutePath());

        FileLogListener listener = new FileLogListener(openSearchService, objectMapper, filePath);

        this.tailer = Tailer.builder()
                .setFile(file)
                .setTailerListener(listener)
                .setDelayDuration(Duration.ofMillis(READ_FILE_DURATION_MS))
                .setTailFromEnd(true)
                .get();

        tailer.run();
    }

    @PreDestroy
    public void stop() {
        if (tailer != null) {
            log.info("Application is shutting down. Closing tailer.");
            tailer.close();
        }
    }
}