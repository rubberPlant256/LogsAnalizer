package org.strongcat.repository.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class LogRepositoryImplTest {

    @Autowired
    private LogRepositoryImpl logRepository;

    @MockitoBean
    private OpenSearchClient openSearchClient;

    @Test
    @DisplayName("Взаимодействие классов")
    void shouldExecuteFullFlowSuccessfully() throws IOException {
        // given
        String fieldName = "level";
        String queryText = "ERROR";
        int size = 10;
        List<String> cursor = null;

        HitsMetadata<LogEntry> hitsMetadata = new HitsMetadata.Builder<LogEntry>().hits(List.of()).build();
        SearchResponse<LogEntry> mockResponse = new SearchResponse.Builder<LogEntry>()
                .took(1L)
                .timedOut(false)
                .shards(s -> s.total(1).successful(1).failed(0).skipped(0))
                .hits(hitsMetadata)
                .build();

        when(openSearchClient.search(any(SearchRequest.class), eq(LogEntry.class)))
                .thenReturn(mockResponse);

        // when
        PagedLogResponse response = logRepository.findByFieldAndValue(fieldName, queryText, size, cursor);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getLogs()).isEmpty();

        verify(openSearchClient).search(any(SearchRequest.class), eq(LogEntry.class));
    }

    @Test
    @DisplayName("Перехватывает IOException и заворачивает в RuntimeException")
    void shouldThrowRuntimeExceptionWhenOpenSearchThrowsIOException() throws IOException {
        // given
        doThrow(new IOException("Connection refused"))
                .when(openSearchClient).search(any(SearchRequest.class), eq(LogEntry.class));

        // when & then
        assertThatThrownBy(() -> logRepository.findByFieldAndValue("message", "test",
                10, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Search after failed")
                .hasCauseInstanceOf(IOException.class);
    }
}