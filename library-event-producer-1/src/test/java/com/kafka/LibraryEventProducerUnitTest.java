package com.kafka;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

	@Mock
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Spy
	ObjectMapper objectMapper;

	@InjectMocks
	LibraryEventProducer eventProducer;

	@Test
	void sendLibraryEvent_Approach2_failure() throws JsonProcessingException, InterruptedException, ExecutionException {
		// given
		Book book = new Book(123, "spring with kafka", "kamal");
		LibraryEvent libraryEvent = new LibraryEvent(null, book, LibraryEventType.NEW);

		// when
		SettableListenableFuture future = new SettableListenableFuture();
		future.setException(new RuntimeException("Exception calling kafka "));

		when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
		// then

		assertThrows(Exception.class, () -> eventProducer.sendLibraryEvent_Approach2(libraryEvent).get());

	}

	@Test
	void sendLibraryEvent_Approach2_success() throws JsonProcessingException, InterruptedException, ExecutionException {
		// given
		Book book = new Book(123, "spring with kafka", "kamal");
		LibraryEvent libraryEvent = new LibraryEvent(null, book, LibraryEventType.NEW);

		// when
		SettableListenableFuture future = new SettableListenableFuture();

		String record = objectMapper.writeValueAsString(libraryEvent);

		ProducerRecord<Integer, String> producerRecord = new ProducerRecord<Integer, String>("library-events",
				libraryEvent.getLibraryEventId(), record);
 
		RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1), 1, 1, 342,
				System.currentTimeMillis(), 1, 2);

		SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
		future.set(sendResult);

		when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
		// then

		ListenableFuture<SendResult<Integer, String>> listenableFuture = eventProducer
				.sendLibraryEvent_Approach2(libraryEvent);
		SendResult<Integer, String> sendResult1 = listenableFuture.get();

		assert sendResult1.getRecordMetadata().partition() == 1;

	}

}
