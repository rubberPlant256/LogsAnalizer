package org.strongcat;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("kafka")
public abstract class BaseIntegrationTest {

  static final KafkaContainer kafka;

  static {
    kafka =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withEmbeddedZookeeper();

    kafka.start();
    System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
  }

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }
}
