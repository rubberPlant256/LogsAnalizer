package org.strongcat.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.strongcat.data.LogEntry;
import org.strongcat.service.OpenSearchService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class OpenSearchIntegrationTest {

    private static final String ADMIN_PASSWORD = "Dgjjd-j!j78";

    @Container
    static final OpenSearchContainer<?> opensearch = new OpenSearchContainer<>(
            DockerImageName.parse("opensearchproject/opensearch:2.12.0")
    )
            .withEnv("discovery.type", "single-node")
            .withEnv("OPENSEARCH_INITIAL_ADMIN_PASSWORD", ADMIN_PASSWORD);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.opensearch.host", opensearch::getHost);
        registry.add("app.opensearch.port", () -> opensearch.getMappedPort(9200));
        registry.add("app.opensearch.username", () -> "admin");
        registry.add("app.opensearch.password", () -> ADMIN_PASSWORD);
    }

    @Autowired
    private OpenSearchService openSearchService;

    @Autowired
    private OpenSearchClient openSearchClient;

    @Test
    @DisplayName("Должен успешно сохранить лог и найти его в OpenSearch")
    void shouldSaveAndFindLog() throws IOException {
        // given
        LogEntry entry = new LogEntry();
        entry.setService("test-service");
        entry.setMessage("Integration test message");

        // when
        openSearchService.saveLog(entry);

        // then
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    var response = openSearchClient.search(s -> s
                                    .index("logs*")
                                    .query(q -> q
                                            .match(m -> m
                                                    .field("message")
                                                    .query(v -> v.stringValue("Integration"))
                                            )
                                    ),
                            LogEntry.class
                    );

                    assertThat(response.hits().total().value()).isGreaterThan(0);
                });
    }
}