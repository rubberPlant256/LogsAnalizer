package org.strongcat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.strongcat.data.LogEntry;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenSearchServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @InjectMocks
    private OpenSearchService openSearchService;

    private LogEntry testEntry;

    @BeforeEach
    void setUp() {
        testEntry = LogEntry.builder()
                .service("payment-service")
                .message("Process started")
                .level("INFO")
                .build();
    }

    @Test
    @DisplayName("Должен успешно вызвать клиент OpenSearch с правильным индексом и данными")
    void shouldSaveLogSuccessfully() throws IOException {
        // given
        String expectedIndex = "logs-payment-service-" + LocalDate.now();

        // when
        openSearchService.saveLog(testEntry);

        // then
        ArgumentCaptor<Function> captor = ArgumentCaptor.forClass(Function.class);
        verify(openSearchClient).index(captor.capture());
        IndexRequest.Builder<Map<String, Object>> builder = new IndexRequest.Builder<>();

        captor.getValue().apply(builder);
        IndexRequest<Map<String, Object>> request = builder.build();

        assertThat(request.index()).isEqualTo(expectedIndex);
        assertThat(request.document())
                .containsEntry("service", "payment-service")
                .containsEntry("message", "Process started");
    }

    @Test
    @DisplayName("Должен пробрасывать исключение, если клиент OpenSearch выдал ошибку")
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionWhenClientFails() throws IOException {
        // given
        when(openSearchClient.index(any(Function.class)))
                .thenThrow(new IOException("OpenSearch connection refused"));

        // when & then
        assertThatThrownBy(() -> openSearchService.saveLog(testEntry))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("OpenSearch connection refused");
    }
}