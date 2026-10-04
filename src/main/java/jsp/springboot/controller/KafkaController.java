package jsp.springboot.controller;



import jsp.springboot.service.KafkaBatchPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
@Slf4j
public class KafkaController {

    private final KafkaBatchPublisher publisher;

    public KafkaController(KafkaBatchPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publish() {
        log.info("Publishing kafka batch to cluster");
        publisher.publishBatch();
        log.info("Published kafka batch to cluster");
        return ResponseEntity.ok(
                "Tutorial + Product events published"
        );
    }
}