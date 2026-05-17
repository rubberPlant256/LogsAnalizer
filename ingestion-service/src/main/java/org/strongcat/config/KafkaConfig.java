package org.strongcat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import static org.strongcat.constant.KafkaSettings.RETRY_INTERVAL;
import static org.strongcat.constant.KafkaSettings.RETRY_MAX_ATTEMPTS;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        FixedBackOff backOff = new FixedBackOff(RETRY_INTERVAL, RETRY_MAX_ATTEMPTS);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.error("Retry attempt #{} for message: {}. Exception: {}",
                    deliveryAttempt, record.value(), ex.getMessage());
        });

        return handler;
    }
}