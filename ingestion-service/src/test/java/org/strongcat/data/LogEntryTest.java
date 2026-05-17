package org.strongcat.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LogEntryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Должен корректно собирать все поля, включая произвольные (extraFields)")
    void shouldReturnAllFieldsIncludingExtra() {
        // given
        LogEntry entry = LogEntry.builder()
                .timestamp("2023-10-27T10:00:00Z")
                .level("INFO")
                .loggerName("org.strongcat.Main")
                .threadName("main")
                .message("Test message")
                .service("auth-service")
                .build();

        entry.addExtraField("user_id", "123");
        entry.addExtraField("trace_id", "abc-789");

        // when
        Map<String, Object> result = entry.getAllFields();

        // then
        assertThat(result)
                .containsEntry("@timestamp", "2023-10-27T10:00:00Z")
                .containsEntry("level", "INFO")
                .containsEntry("user_id", "123")
                .containsEntry("trace_id", "abc-789")
                .hasSize(8); // 6 основных + 2 дополнительных
    }

    @Test
    @DisplayName("Jackson должен десериализовать известные и неизвестные поля")
    void shouldDeserializeFromJsonCorrectly() throws Exception {
        // given
        String json = """
                {
                  "@timestamp": "2023-10-27T10:00:00Z",
                  "level": "ERROR",
                  "message": "Critical error",
                  "unknown_field": "some-value",
                  "custom_metadata": 42
                }
                """;

        // when
        LogEntry entry = objectMapper.readValue(json, LogEntry.class);

        // then
        assertThat(entry.getLevel()).isEqualTo("ERROR");
        assertThat(entry.getMessage()).isEqualTo("Critical error");
        assertThat(entry.getTimestamp()).isEqualTo("2023-10-27T10:00:00Z");

        assertThat(entry.getExtraFields())
                .containsEntry("unknown_field", "some-value")
                .containsEntry("custom_metadata", 42);
    }

    @Test
    @DisplayName("getAllFields не должен бросать NPE, если extraFields пуст")
    void getAllFieldsShouldWorkWithEmptyExtraFields() {
        // given
        LogEntry entry = new LogEntry();
        entry.setLevel("DEBUG");

        // when
        Map<String, Object> result = entry.getAllFields();

        // then
        assertThat(result).containsKey("level");
        assertThat(result.get("level")).isEqualTo("DEBUG");
        assertThat(result.get("message")).isNull();
    }

    @Test
    @DisplayName("addExtraField должен инициализировать мапу, если она null")
    void addExtraFieldShouldHandleNullMap() {
        // given
        LogEntry entry = new LogEntry();
        entry.setExtraFields(null);

        // when
        entry.addExtraField("key", "value");

        // then
        assertThat(entry.getExtraFields()).containsEntry("key", "value");
    }
}