package org.strongcat.service.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.strongcat.data.LogEntry;
import org.strongcat.service.OpenSearchService;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class KafkaLogSourceIT {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private OpenSearchService openSearchService;

    @Value("${app.kafka.topic}")
    private String topic;

    @Test
    @DisplayName("Получает сообщение из kafka и вызывает метод отправки в openSearch")
    void shouldReceiveMessageFromKafkaAndSaveToOpenSearch() {
        // given
        LogEntry entry = new LogEntry();
        entry.setMessage("Valid JSON");

        // when
        kafkaTemplate.send(topic, entry);

        // then
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
            verify(openSearchService).saveLog(any(LogEntry.class));
        });
    }

    @Test
    @DisplayName("openSearch недоступен, делаем 3 попытки отправки")
    void shouldRetryWhenOpenSearchFails() throws IOException {
        // given
        LogEntry entry = new LogEntry();
        entry.setMessage("Retry Message");

        doThrow(new RuntimeException("OpenSearch Down"))
                .when(openSearchService).saveLog(any());

        // when
        kafkaTemplate.send(topic, entry);

        // then
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            verify(openSearchService, atLeast(2)).saveLog(any());
        });
    }


    @Test
    @DisplayName("Не валидный JSON не попадет в openSearch")
    void shouldSendToDltWhenDeserializationFails() {
        // given
        String invalidJson = "{\"invalid\": \"data\" - missing bracket";

        // when
        kafkaTemplate.send(topic, invalidJson);

        // then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            verify(openSearchService, never()).saveLog(any());
        });
    }
}