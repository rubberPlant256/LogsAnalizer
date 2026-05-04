package org.strongcat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.strongcat.service.OpenSearchService;
import org.strongcat.service.source.FileLogSource;
import org.strongcat.service.source.KafkaLogSource;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionalLoadingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                KafkaLogSource.class,
                FileLogSource.class
            )
            .withBean(OpenSearchService.class, () -> Mockito.mock(OpenSearchService.class))
            .withBean(ObjectMapper.class, () -> Mockito.mock(ObjectMapper.class));

    @Test
    @DisplayName("При типе источника KAFKA должен создаваться только KafkaLogSource")
    void shouldLoadOnlyKafkaSource() {
        contextRunner
                .withPropertyValues("app.source.type=kafka")
                .run(context -> {
                    assertThat(context).hasSingleBean(KafkaLogSource.class);
                    assertThat(context).doesNotHaveBean(FileLogSource.class);
                });
    }

    @Test
    @DisplayName("При типе источника FILE должен создаваться только FileLogSource")
    void shouldLoadOnlyFileSource() {
        contextRunner
                .withPropertyValues("app.source.type=file")
                .run(context -> {
                    assertThat(context).hasSingleBean(FileLogSource.class);
                    assertThat(context).doesNotHaveBean(KafkaLogSource.class);
                });
    }

    @Test
    @DisplayName("Если тип источника не указан, ни один бин не должен создаться")
    void shouldLoadNoBeansWhenPropertyMissing() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(KafkaLogSource.class);
                    assertThat(context).doesNotHaveBean(FileLogSource.class);
                });
    }
}