package org.strongcat.repository.util;

import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Component;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;

import java.util.List;
import java.util.Objects;

@Component
public class LogResponseMapper {

    public PagedLogResponse toPagedLogResponse(SearchResponse<LogEntry> response) {
        List<Hit<LogEntry>> hits = response.hits().hits();

        List<LogEntry> logs = hits.stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        List<String> nextCursor = hits.isEmpty() ? null : hits.getLast().sort();

        return new PagedLogResponse(logs, nextCursor);
    }
}