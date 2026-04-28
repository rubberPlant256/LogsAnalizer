package org.strongcat.service;

import static net.logstash.logback.argument.StructuredArguments.fields;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.strongcat.constant.LogLevel;
import org.strongcat.constant.LogMessageData;
import org.strongcat.data.LogMessage;

@Service
@Slf4j
public class LogGeneratorService {

  @Scheduled(fixedRate = 5000)
  public void generate() {
    LogLevel level = LogLevel.getRandom();
    String message = LogMessageData.getRandomMessage();

    LogMessage meta =
        LogMessage.builder()
            .id(UUID.randomUUID())
            .traceId(UUID.randomUUID().toString().substring(0, 8))
            .build();

    logToConsole(level, message, meta);
  }

  private void logToConsole(LogLevel level, String message, LogMessage meta) {
    switch (level) {
      case INFO -> log.info(message, fields(meta));
      case ERROR -> log.error(message, fields(meta));
      case WARN -> log.warn(message, fields(meta));
      case DEBUG -> log.debug(message, fields(meta));
      case TRACE -> log.trace(message, fields(meta));
    }
  }
}
