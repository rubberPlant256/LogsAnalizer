package org.strongcat.service.source;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.strongcat.data.LogEntry;
import org.strongcat.service.OpenSearchService;
import org.strongcat.service.source.impl.LogSource;

import java.io.IOException;

import static org.strongcat.constant.SourceType.KAFKA_SOURCE;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnProperty(name = "app.source.type", havingValue = KAFKA_SOURCE)
public class KafkaLogSource implements LogSource {

    private final OpenSearchService openSearchService;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.groupId}")
    public void listen(LogEntry logEntry) throws IOException {
        openSearchService.saveLog(logEntry);
    }

    @Override
    public void startListening() {
        log.info("KafkaLogSource started and waiting for messages");
    }
}