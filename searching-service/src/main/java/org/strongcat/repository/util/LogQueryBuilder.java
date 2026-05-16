package org.strongcat.repository.util;

import org.opensearch.client.opensearch._types.FieldSort;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogQueryBuilder {

    private static final String INDEX_PATTERN = "logs-*";
    private static final String FUZZINESS_VALUE = "AUTO";
    private static final String FUZZY_SEARCH_SUFFIX = "~";

    private static final SortOptions TIMESTAMP_SORT = new SortOptions.Builder()
            .field(new FieldSort.Builder().field("@timestamp").order(SortOrder.Desc).build())
            .build();

    private static final SortOptions ID_SORT = new SortOptions.Builder()
            .field(new FieldSort.Builder().field("_id").order(SortOrder.Desc).build())
            .build();

    public SearchRequest buildSearchRequest(String fieldName, String queryText, int size, List<String> cursor) {
        Query query = buildFlexibleQuery(fieldName, queryText);

        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(INDEX_PATTERN)
                .query(query)
                .size(size)
                .sort(TIMESTAMP_SORT)
                .sort(ID_SORT);

        if (cursor != null && !cursor.isEmpty()) {
            builder.searchAfter(cursor);
        }

        return builder.build();
    }

    private Query buildFlexibleQuery(String fieldName, String queryText) {
        boolean hasField = fieldName != null && !fieldName.isBlank();
        boolean hasQuery = queryText != null && !queryText.isBlank();

        if (!hasField && !hasQuery) {
            return buildMatchAllQuery();
        }
        if (hasField && hasQuery) {
            return buildMatchQuery(fieldName, queryText);
        }
        if (hasQuery) {
            return buildQueryStringQuery(queryText);
        }
        return buildExistsQuery(fieldName);
    }

    private Query buildMatchAllQuery() {
        MatchAllQuery matchAll = new MatchAllQuery.Builder().build();
        return new Query.Builder().matchAll(matchAll).build();
    }

    private Query buildMatchQuery(String fieldName, String queryText) {
        MatchQuery match = new MatchQuery.Builder()
                .field(fieldName)
                .query(FieldValue.of(queryText))
                .fuzziness(FUZZINESS_VALUE)
                .fuzzyTranspositions(true)
                .build();
        return new Query.Builder().match(match).build();
    }

    private Query buildQueryStringQuery(String queryText) {
        QueryStringQuery queryString = new QueryStringQuery.Builder()
                .query(queryText + FUZZY_SEARCH_SUFFIX)
                .build();
        return new Query.Builder().queryString(queryString).build();
    }

    private Query buildExistsQuery(String fieldName) {
        ExistsQuery exists = new ExistsQuery.Builder()
                .field(fieldName)
                .build();
        return new Query.Builder().exists(exists).build();
    }
}