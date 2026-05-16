package org.strongcat.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;
import org.strongcat.service.LogSearchService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = LogSearchController.class
)
class LogSearchControllerTest {

    private RestTestClient restClient;

    @MockitoBean
    private LogSearchService logSearchService;

    @BeforeEach
    void setUp() {
        this.restClient = RestTestClient.bindToController(
                new LogSearchController(logSearchService)).build();
    }

    @Test
    @DisplayName("Должен вернуть 200 OK и JSON с логами при передаче всех параметров")
    void shouldReturnLogsWhenAllParametersAreProvided() {
        // given
        LogEntry logEntry = new LogEntry("2026-05-16T12:00:00Z", "ERROR", "Logger",
                "main", "Test error", "auth-service", null);
        PagedLogResponse mockResponse = new PagedLogResponse(List.of(logEntry), List.of("12345", "id-1"));

        when(logSearchService.search(eq("level"), eq("ERROR"), eq(10), eq(List.of("111", "222"))))
                .thenReturn(mockResponse);

        // when & then
        restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/logs/search")
                        .queryParam("fieldName", "level")
                        .queryParam("query", "ERROR")
                        .queryParam("size", 10)
                        .queryParam("cursor", "111,222")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.logs").isArray()
                .jsonPath("$.logs[0].level").isEqualTo("ERROR")
                .jsonPath("$.logs[0].message").isEqualTo("Test error")
                .jsonPath("$.logs[0].service").isEqualTo("auth-service")
                .jsonPath("$.nextCursor").isArray()
                .jsonPath("$.nextCursor[0]").isEqualTo("12345")
                .jsonPath("$.nextCursor[1]").isEqualTo("id-1");

        verify(logSearchService).search("level", "ERROR", 10, List.of("111", "222"));
    }

    @Test
    @DisplayName("Должен возвращать 500 Internal Server Error, если сервис выбросил исключение")
    void shouldReturnInternalServerErrorWhenServiceFails() {
        // given
        when(logSearchService.search(any(), any(), anyInt(), any()))
                .thenThrow(new RuntimeException("OpenSearch is down"));

        // when & then
        restClient.get()
                .uri("/api/v1/logs/search")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}