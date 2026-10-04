package jsp.springboot.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import jsp.springboot.dto.EventPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic}")
    private String topic;
    private final Tracer tracer =
            GlobalOpenTelemetry.getTracer("jsp.springboot.service.KafkaEventProducer");
    public KafkaEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(EventPayload event) {
        Span span = tracer.spanBuilder("tutorial-service.update-tutorial")
                .startSpan();
        try (var scope = span.makeCurrent()) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("Publishing kafka batch to cluster for Tutorial {} to topic {}", json, topic);
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(
                            topic,
                            event.getRecordId().toString(),
                            json
                    );

            record.headers().add(
                    "event-type",
                    event.getEventType().getBytes(StandardCharsets.UTF_8)
            );

            kafkaTemplate.send(record);
            log.info("Published to cluster for Tutorial {}", json);
            span.setStatus(StatusCode.OK);
        } catch (JsonProcessingException e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to publish tutorial update ");
            throw new RuntimeException(
                    "Failed to serialize Kafka event",
                    e
            );
        }
        } catch (Exception e) {

            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to publish tutorial update");

            throw e;

        } finally {
            span.end();
        }
    }
}