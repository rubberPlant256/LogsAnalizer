package org.strongcat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Service;
import org.strongcat.data.LogEntry;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchService {

    private final OpenSearchClient client;

    public void saveLog(LogEntry entry) throws IOException {
        String indexName = nameIndex(entry.getService(),
                                     LocalDate.now());

        client.index(i -> i
                .index(indexName)
                .document(entry.getAllFields())
        );

        log.info("Log successfully sent to index: {}", indexName);
    }

    private String nameIndex(String serviceName, LocalDate date) {
        return String.format("logs-%s-%s", Objects.requireNonNullElse(serviceName, "unknown"), date);
    }
}