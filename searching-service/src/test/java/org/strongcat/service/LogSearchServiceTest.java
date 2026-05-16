package org.strongcat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.strongcat.data.LogEntry;
import org.strongcat.dto.PagedLogResponse;
import org.strongcat.repository.LogRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogSearchServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogSearchService logSearchService;

    @Test
    @DisplayName("Должен вызвать репозиторий с переданными параметрами и вернуть его ответ")
    void shouldCallRepositoryWithExactParametersAndReturnResponse() {
        // given
        String fieldName = "level";
        String query = "ERROR";
        int size = 10;
        List<String> cursor = List.of("171587123", "id-1");

        List<LogEntry> mockLogs = List.of(new LogEntry());
        PagedLogResponse expectedResponse = new PagedLogResponse(mockLogs, List.of("171587124", "id-2"));

        when(logRepository.findByFieldAndValue(fieldName, query, size, cursor))
                .thenReturn(expectedResponse);

        // when
        PagedLogResponse actualResponse = logSearchService.search(fieldName, query, size, cursor);

        // then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getLogs()).hasSize(1);
        assertThat(actualResponse.getNextCursor()).containsExactly("171587124", "id-2");

        verify(logRepository, times(1)).findByFieldAndValue(fieldName, query, size, cursor);
    }

    @Test
    @DisplayName("Должен пробросить исключение наверх, если репозиторий выбросил ошибку")
    void shouldPropagateExceptionWhenRepositoryFails() {
        // given
        String fieldName = "message";
        String query = "timeout";
        int size = 5;
        List<String> cursor = null;

        when(logRepository.findByFieldAndValue(fieldName, query, size, cursor))
                .thenThrow(new RuntimeException("Search after failed"));

        // when & then
        assertThatThrownBy(() -> logSearchService.search(fieldName, query, size, cursor))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Search after failed");

        verify(logRepository, times(1)).findByFieldAndValue(fieldName, query, size, cursor);
    }
}