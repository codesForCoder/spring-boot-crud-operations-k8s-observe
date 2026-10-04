package jsp.springboot;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class SpringBootCrudOperationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCrudOperationApplication.class, args);
	}

	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}


	@Bean
	public KafkaTemplate<String, String> kafkaTemplate(
			ProducerFactory<String, String> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
}
