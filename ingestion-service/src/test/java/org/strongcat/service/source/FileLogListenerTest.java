package org.strongcat.service.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.strongcat.data.LogEntry;
import org.strongcat.service.OpenSearchService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileLogListenerTest {

    @Mock
    private OpenSearchService openSearchService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private final String filePath = "test.log";

    @InjectMocks
    private FileLogListener fileLogListener;

    @Test
    @DisplayName("Должен успешно распарсить строку и отправить в OpenSearch")
    void shouldProcessValidLine() throws IOException {
        // given
        String jsonLine = "{\"service\":\"test\", \"message\":\"hello\"}";

        // when
        fileLogListener.handle(jsonLine);

        // then
        verify(openSearchService, times(1)).saveLog(any(LogEntry.class));
    }

    @Test
    @DisplayName("Должен игнорировать пустые строки")
    void shouldIgnoreEmptyLines() throws IOException {
        // when
        fileLogListener.handle("");
        fileLogListener.handle("   ");
        fileLogListener.handle((String) null);

        // then
        verifyNoInteractions(openSearchService);
    }

    @Test
    @DisplayName("Не должен пробрасывать исключение, если JSON невалиден")
    void shouldHandleInvalidJsonGracefully() throws IOException {
        // given
        String invalidJson = "{ this is not a json }";

        // when & then (не должно выкинуть исключение)
        fileLogListener.handle(invalidJson);

        verifyNoInteractions(openSearchService);
    }

    @Test
    @DisplayName("Должен логировать ошибку, если OpenSearchService недоступен")
    void shouldHandleServiceError() throws IOException {
        // given
        String jsonLine = "{\"service\":\"test\"}";
        doThrow(new IOException("OpenSearch connection refused")).when(openSearchService).saveLog(any());

        // when
        fileLogListener.handle(jsonLine);

        // then
        verify(openSearchService).saveLog(any());
    }
}