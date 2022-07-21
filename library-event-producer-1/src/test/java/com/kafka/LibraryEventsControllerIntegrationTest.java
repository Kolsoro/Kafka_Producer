package com.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import lombok.val;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}" })
public class LibraryEventsControllerIntegrationTest {

	@Autowired
	TestRestTemplate restTemplate;
	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;

	private Consumer<Integer, String> consumer;

	@BeforeEach
	void setUp() {
		HashMap<String, Object> configs = new HashMap<>(
				KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));

		consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
				.createConsumer();
		embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
	}

	@AfterEach 
	void tearDown() {
		consumer.close();
	}

	@Test
	@Timeout(5)
	void postLibraryEvent() throws InterruptedException {
		Book book = new Book(123, "spring with kafka", "kamal");
		LibraryEvent libraryEvent = new LibraryEvent(null, book, LibraryEventType.NEW);

		HttpHeaders headers = new HttpHeaders();
		headers.set("content-type", MediaType.APPLICATION_JSON.toString());
		HttpEntity<LibraryEvent> request = new HttpEntity<LibraryEvent>(libraryEvent, headers);

		ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST,
				request, LibraryEvent.class);

		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

		ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
		String value = consumerRecord.value();

		String expectedValue = "{\"libraryEventId\":null,\"book\":{\"bookId\":123,\"bookName\":"
				+ "\"spring with kafka\",\"bookAuthor\":\"kamal\"},\"libraryEventType\":\"NEW\"}";
		assertEquals(expectedValue, value);
	}

}
