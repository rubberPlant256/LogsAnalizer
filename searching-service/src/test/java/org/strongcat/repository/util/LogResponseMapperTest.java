package org.strongcat.repository.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;
import org.strongcat.mapper.LogResponseMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogResponseMapperTest {

    private LogResponseMapper responseMapper;

    @BeforeEach
    void setUp() {
        responseMapper = new LogResponseMapper();
    }

    @Test
    @DisplayName("Должен смапить хиты и вернуть курсор из последнего элемента")
    void shouldMapResponseWithHitsAndReturnLastSortAsCursor() {
        // given
        LogEntry log1 = new LogEntry("2026-05-16T12:00:00Z", "INFO", "Log1",
                "main", "Message 1", "auth-service", null);
        LogEntry log2 = new LogEntry("2026-05-16T12:01:00Z", "ERROR", "Log2",
                "main", "Message 2", "auth-service", null);

        Hit<LogEntry> hit1 = new Hit.Builder<LogEntry>()
                .id("id-1")
                .index("logs-2026")
                .source(log1)
                .sort(List.of("1715871230000", "id-1"))
                .build();

        Hit<LogEntry> hit2 = new Hit.Builder<LogEntry>()
                .id("id-2")
                .index("logs-2026")
                .source(log2)
                .sort(List.of("1715871290000", "id-2"))
                .build();

        SearchResponse<LogEntry> searchResponse = createSearchResponse(List.of(hit1, hit2));

        // when
        PagedLogResponse result = responseMapper.toPagedLogResponse(searchResponse);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLogs())
                .hasSize(2)
                .containsExactly(log1, log2);

        assertThat(result.getNextCursor())
                .containsExactly("1715871290000", "id-2");
    }

    @Test
    @DisplayName("Должен вернуть пустой список и null-курсор, если OpenSearch вернул 0 результатов")
    void shouldReturnEmptyLogsAndNullCursorWhenHitsAreEmpty() {
        // given
        SearchResponse<LogEntry> emptyResponse = createSearchResponse(List.of());

        // when
        PagedLogResponse result = responseMapper.toPagedLogResponse(emptyResponse);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLogs()).isEmpty();
        assertThat(result.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("Должен игнорировать хиты, у которых source равен null, но корректно брать курсор")
    void shouldFilterOutNullSourcesButStillKeepCorrectCursor() {
        // given
        LogEntry validLog = new LogEntry("2026-05-16T12:00:00Z", "INFO", "Log1",
                "main", "Valid", "service", null);

        Hit<LogEntry> hitWithNullSource = new Hit.Builder<LogEntry>()
                .id("id-1")
                .index("logs-2026")
                .source(null)
                .sort(List.of("1000", "id-1"))
                .build();

        Hit<LogEntry> hitWithValidSource = new Hit.Builder<LogEntry>()
                .id("id-2")
                .index("logs-2026")
                .source(validLog)
                .sort(List.of("2000", "id-2"))
                .build();

        SearchResponse<LogEntry> searchResponse = createSearchResponse(List.of(hitWithNullSource, hitWithValidSource));

        // when
        PagedLogResponse result = responseMapper.toPagedLogResponse(searchResponse);

        // then
        assertThat(result.getLogs())
                .hasSize(1)
                .containsExactly(validLog);

        assertThat(result.getNextCursor())
                .containsExactly("2000", "id-2");
    }

    private SearchResponse<LogEntry> createSearchResponse(List<Hit<LogEntry>> hitsList) {
        HitsMetadata<LogEntry> hitsMetadata = new HitsMetadata.Builder<LogEntry>()
                .hits(hitsList)
                .build();

        return new SearchResponse.Builder<LogEntry>()
                .took(10L)
                .timedOut(false)
                .shards(s -> s.total(1).successful(1).failed(0).skipped(0))
                .hits(hitsMetadata)
                .build();
    }
}