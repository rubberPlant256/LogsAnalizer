package org.strongcat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class SearchingServiceApplicationIT {

    @MockitoBean
    private OpenSearchClient openSearchClient;

    @Test
    @DisplayName("Проверка успешного старта Spring-контекста приложения")
    void contextLoads() {
    }
}