package org.strongcat.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Repository;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;
import org.strongcat.repository.LogRepository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepositoryImpl implements LogRepository {

    private final OpenSearchClient client;

    private static final String INDEX_PATTERN = "logs-*";

    @Override
    public PagedLogResponse findByFieldAndValue(String fieldName, String queryText, int size, List<String> cursor) {
        try {
            Query query = buildFlexibleQuery(fieldName, queryText);

            SearchResponse<LogEntry> response = client.search(s -> {
                s.index(INDEX_PATTERN)
                        .query(query)
                        .size(size)
                        // Сортировка по двум полям обязательна для уникальности курсора
                        .sort(sort -> sort.field(f -> f.field("@timestamp").order(SortOrder.Desc)))
                        .sort(sort -> sort.field(f -> f.field("_id").order(SortOrder.Desc)));

                // Если курсор передан — используем его
                if (cursor != null && !cursor.isEmpty()) {
                    s.searchAfter(cursor);
                }
                return s;
            }, LogEntry.class);

            List<Hit<LogEntry>> hits = response.hits().hits();

            List<LogEntry> logs = hits.stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

            List<String> nextCursor = null;
            if (!hits.isEmpty()) {
                nextCursor = hits.get(hits.size() - 1).sort();
            }

            return new PagedLogResponse(logs, nextCursor);

        } catch (IOException e) {
            throw new RuntimeException("Search after failed", e);
        }
    }

    private Query buildFlexibleQuery(String fieldName, String queryText) {
        boolean hasField = fieldName != null && !fieldName.isBlank();
        boolean hasQuery = queryText != null && !queryText.isBlank();

        if (!hasField && !hasQuery) {
            MatchAllQuery matchAllQuery = new MatchAllQuery.Builder().build();

            return new Query.Builder()
                    .matchAll(matchAllQuery)
                    .build();
        }

        if (hasField && hasQuery) {
            return Query.of(q -> q.match(m -> m
                    .field(fieldName)
                    .query(FieldValue.of(queryText))
                    .fuzziness("AUTO") // <-- Магия здесь
                    .fuzzyTranspositions(true) // Позволяет менять буквы местами (teh -> the)
            ));
        }

        // 2. Полнотекстовый поиск по всем полям
        if (hasQuery) {
            return Query.of(q -> q.queryString(qs -> qs
                    .query(queryText + "~") // Тильда в конце включает fuzzy для queryString
            ));
        }

        return Query.of(q -> q.exists(e -> e.field(fieldName)));
        // 2. Если есть и поле, и текст -> ищем текст в конкретном поле
//        if (hasField && hasQuery) {
//            return Query.of(q -> q.match(m -> m
//                    .field(fieldName)
//                    .query(v -> v.stringValue(queryText))
//            ));
//        }
//        // 1. Создаем внутреннюю часть (сам MatchQuery)
////        MatchQuery matchQuery = new MatchQuery.Builder()
////                .field(fieldName)                        // Указываем поле
////                .query(FieldValue.of(queryText))        // Указываем значение
////                .build();
////
////// 2. Оборачиваем его в универсальный объект Query
////        Query query = new Query.Builder()
////                .match(matchQuery)
////                .build();
////
////        return query;
//
//        // 3. Если есть только текст (без поля) -> ищем по всем полям (Full-text search)
//        if (hasQuery) {
//            return Query.of(q -> q.queryString(qs -> qs.query(queryText)));
//        }
//
//        // 4. Если есть только название поля (без текста) -> проверяем существование этого поля
//        return Query.of(q -> q.exists(e -> e.field(fieldName)));
    }
}