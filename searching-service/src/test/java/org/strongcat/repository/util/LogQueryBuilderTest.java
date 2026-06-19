package org.strongcat.repository.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.strongcat.repository.impl.LogQueryBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogQueryBuilderTest {

    private LogQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        queryBuilder = new LogQueryBuilder();
    }

    @Test
    @DisplayName("Должен создать MatchAll query, если переданы пустые fieldName и queryText")
    void shouldBuildMatchAllQueryWhenFieldsAreEmpty() {
        // when
        SearchRequest request = queryBuilder.buildSearchRequest(null, "   ", 10, null);

        // then
        assertThat(request.index()).containsExactly("logs-*");
        assertThat(request.size()).isEqualTo(10);
        assertThat(request.query().isMatchAll()).isTrue();

        assertCommonSortOptions(request);
    }

    @Test
    @DisplayName("Должен создать Match query с fuzziness, если переданы и fieldName, и queryText")
    void shouldBuildMatchQueryWhenBothFieldAndQueryPresent() {
        // given
        String field = "message";
        String text = "error_text";

        // when
        SearchRequest request = queryBuilder.buildSearchRequest(field, text, 20, null);

        // then
        assertThat(request.query().isMatch()).isTrue();
        var matchQuery = request.query().match();
        assertThat(matchQuery.field()).isEqualTo(field);
        assertThat(matchQuery.query().stringValue()).isEqualTo(text);
        assertThat(matchQuery.fuzziness()).isEqualTo("AUTO");
        assertThat(matchQuery.fuzzyTranspositions()).isTrue();

        assertCommonSortOptions(request);
    }

    @Test
    @DisplayName("Должен создать QueryString query с суффиксом ~, если передано только queryText")
    void shouldBuildQueryStringQueryWhenOnlyQueryTextPresent() {
        // given
        String text = "exception";

        // when
        SearchRequest request = queryBuilder.buildSearchRequest(null, text, 15, null);

        // then
        assertThat(request.query().isQueryString()).isTrue();
        var queryStringQuery = request.query().queryString();
        assertThat(queryStringQuery.query()).isEqualTo("exception~");

        assertCommonSortOptions(request);
    }

    @Test
    @DisplayName("Должен создать Exists query, если передано только fieldName")
    void shouldBuildExistsQueryWhenOnlyFieldNamePresent() {
        // given
        String field = "service_name";

        // when
        SearchRequest request = queryBuilder.buildSearchRequest(field, "", 5, null);

        // then
        assertThat(request.query().isExists()).isTrue();
        var existsQuery = request.query().exists();
        assertThat(existsQuery.field()).isEqualTo(field);

        assertCommonSortOptions(request);
    }

    @Test
    @DisplayName("Должен добавить search_after, если курсор передан и не пустой")
    void shouldAddSearchAfterWhenCursorIsProvided() {
        // given
        List<String> cursor = List.of("1777714533718", "e88L6J0B173fIQDPFVPY");

        // when
        SearchRequest request = queryBuilder.buildSearchRequest(null, null, 10, cursor);

        // then
        assertThat(request.searchAfter()).containsExactly("1777714533718", "e88L6J0B173fIQDPFVPY");

        assertCommonSortOptions(request);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Не должен добавлять search_after, если курсор равен null или пустой")
    void shouldNotAddSearchAfterWhenCursorIsEmptyOrNull(List<String> emptyCursor) {
        // when
        SearchRequest request = queryBuilder.buildSearchRequest(null, null, 10, emptyCursor);

        // then
        assertThat(request.searchAfter()).isEmpty();
    }

    private void assertCommonSortOptions(SearchRequest request) {
        assertThat(request.sort()).hasSize(2);

        var firstSort = request.sort().get(0).field();
        assertThat(firstSort.field()).isEqualTo("@timestamp");
        assertThat(firstSort.order()).isEqualTo(SortOrder.Desc);

        var secondSort = request.sort().get(1).field();
        assertThat(secondSort.field()).isEqualTo("_id");
        assertThat(secondSort.order()).isEqualTo(SortOrder.Desc);
    }
}