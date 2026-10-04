package jsp.springboot.service;


import jsp.springboot.dto.EventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@Slf4j
public class KafkaBatchPublisher {

    private final KafkaEventProducer producer;

    public KafkaBatchPublisher(KafkaEventProducer producer) {
        this.producer = producer;
    }

    public void publishBatch() {
        log.info("Publishing kafka batch to cluster for Tutorial");
        List<EventPayload> eventPayloads = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> {
            eventPayloads.add(new EventPayload(
                    UUID.randomUUID().toString(),
                    "TUTORIAL",
                    1,
                    Map.of(
                            "title", "Kafka Tutorial_"+UUID.randomUUID(),
                            "description", "Kafka tracing experiment at : "+ LocalDateTime.now()
                    )
            ));
        });
        log.info("Publishing kafka batch to cluster for Tutorial count  {}", eventPayloads.size());
        eventPayloads.forEach(producer::publish);
        log.info("Published kafka batch to cluster for Tutorial {}", eventPayloads.size());
    }
}
