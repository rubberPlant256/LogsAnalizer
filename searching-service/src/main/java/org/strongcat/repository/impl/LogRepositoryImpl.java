package org.strongcat.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Repository;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;
import org.strongcat.repository.LogRepository;
import org.strongcat.repository.util.LogQueryBuilder;
import org.strongcat.repository.util.LogResponseMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepositoryImpl implements LogRepository {

    private final OpenSearchClient client;
    private final LogQueryBuilder queryBuilder;
    private final LogResponseMapper responseMapper;

    @Override
    public PagedLogResponse findByFieldAndValue(String fieldName, String queryText, int size, List<String> cursor) {
        try {
            SearchRequest searchRequest = queryBuilder.buildSearchRequest(fieldName, queryText, size, cursor);

            SearchResponse<LogEntry> response = client.search(searchRequest, LogEntry.class);

            return responseMapper.toPagedLogResponse(response);

        } catch (IOException e) {
            log.error("OpenSearch execution failed for field: {}, query: {}", fieldName, queryText, e);
            throw new RuntimeException("Search after failed", e);
        }
    }
}
