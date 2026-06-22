package org.strongcat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.MsearchRequest;
import org.opensearch.client.opensearch.core.MsearchResponse;
import org.opensearch.client.opensearch.core.msearch.MultiSearchResponseItem;
import org.opensearch.client.opensearch.core.msearch.RequestItem;
import org.springframework.stereotype.Service;
import org.strongcat.data.Notification;
import org.strongcat.dto.LogSearchResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.strongcat.constant.OpenSearchConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchBatchQueryService {

    private final OpenSearchClient openSearchClient;

    public List<LogSearchResult> executeMultiSearch(List<Notification> rules, Integer seconds) {
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("Forming OpenSearch Multi-Search for interval: {}s. Rules count: {}", seconds, rules.size());

        MsearchRequest.Builder msearchBuilder = new MsearchRequest.Builder();
        Instant fromTime = Instant.now().minusSeconds(seconds);

        for (Notification rule : rules) {
            RequestItem item = buildRequestItem(rule, fromTime);
            msearchBuilder.searches(item);
        }

        try {
            MsearchResponse<Object> response = openSearchClient.msearch(msearchBuilder.build(), Object.class);
            return mapSearchResponse(response.responses(), rules);
        } catch (Exception e) {
            log.error("Failed to execute OpenSearch Multi-Search request", e);
            return buildFallbackResults(rules);
        }
    }

    private RequestItem buildRequestItem(Notification rule, Instant fromTime) {
        Query query = new Query(buildBoolQuery(rule, fromTime));

        return new RequestItem.Builder()
                .header(h -> h.index(INDEX_PATTERN))
                .body(b -> b.query(query).size(SEARCH_SIZE_COUNT_ONLY))
                .build();
    }

    private BoolQuery buildBoolQuery(Notification rule, Instant fromTime) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (rule.getServiceName() != null && !rule.getServiceName().isBlank()) {
            boolBuilder.filter(q -> q
                    .term(t -> t
                            .field(FIELD_SERVICE_KEYWORD)
                            .value(FieldValue.of(rule.getServiceName()))
                    )
            );
        }

        if (rule.getLevel() != null && !rule.getLevel().isBlank()) {
            boolBuilder.filter(q -> q
                    .term(t -> t
                            .field(FIELD_LEVEL_KEYWORD)
                            .value(FieldValue.of(rule.getLevel()))
                    )
            );
        }

        boolBuilder.filter(q -> q
                .range(r -> r
                        .field(FIELD_TIMESTAMP)
                        .gte(JsonData.of(fromTime.toString()))
                )
        );

        if (rule.getTextQuery() != null && !rule.getTextQuery().isBlank()) {
            boolBuilder.must(q -> q
                    .match(m -> m
                            .field(FIELD_MESSAGE)
                            .query(FieldValue.of(rule.getTextQuery()))
                    )
            );
        }

        return boolBuilder.build();
    }

    private List<LogSearchResult> mapSearchResponse(List<MultiSearchResponseItem<Object>> items,
                                                    List<Notification> rules) {
        List<LogSearchResult> results = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            MultiSearchResponseItem<Object> item = items.get(i);
            Notification correspondingRule = rules.get(i);

            if (item.isFailure()) {
                log.error("OpenSearch sub-query failed for service {}: {}",
                        correspondingRule.getServiceName(), item.failure().error().reason());
                results.add(new LogSearchResult(correspondingRule, 0));
            } else {
                long totalHits = item.result().hits().total().value();
                results.add(new LogSearchResult(correspondingRule, totalHits));
            }
        }
        return results;
    }

    private List<LogSearchResult> buildFallbackResults(List<Notification> rules) {
        List<LogSearchResult> fallbackResults = new ArrayList<>();
        for (Notification rule : rules) {
            fallbackResults.add(new LogSearchResult(rule, 0));
        }
        return fallbackResults;
    }
}