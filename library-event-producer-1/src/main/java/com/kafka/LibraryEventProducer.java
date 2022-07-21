package com.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
//@Slf4j
public class LibraryEventProducer {

	private static final Logger logger = LoggerFactory.getLogger(LibraryEventProducer.class);

	@Autowired
	ObjectMapper objectMapper;
	
	String topic="library-events";

	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {

		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);

		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);

		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}

		});

	}

	public ListenableFuture<SendResult<Integer, String>> sendLibraryEvent_Approach2(LibraryEvent libraryEvent) throws JsonProcessingException {

		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);
            
		ProducerRecord<Integer, String> producerRecord = buildProducerRecord(topic,key,value);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}

		});
              return listenableFuture;
	}

	
	
	private ProducerRecord<Integer, String> buildProducerRecord(String topic2, Integer key, String value) {
		
		List<Header> recordHeaders=Arrays.asList(new RecordHeader("event-source", "scanner".getBytes()) );
		return new ProducerRecord<Integer, String>(topic, null, key, value, recordHeaders);
	}
	

	public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent)
			throws Exception {
		Integer key = libraryEvent.getLibraryEventId(); 
		String value = objectMapper.writeValueAsString(libraryEvent);
		SendResult<Integer, String> sendResult=null; //as send result is not visible in  try block
		try {
		  sendResult= kafkaTemplate.sendDefault(key, value).get(1,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException e) {
			logger.error("InterruptedException/ExecutionException Sending the message and exception is :{}",
					e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("InterruptedException/ExecutionException Sending the message and exception is :{}",
					e.getMessage());
			throw e;

		}
		return sendResult;
	}

	protected void handleFailure(Integer key, String value, Throwable ex) {

		logger.error("Error sending the message and the exception is {}", ex.getMessage());
		try {
			throw ex;
		} catch (Throwable th) {
			logger.error("Error in on Failure : {}", th.getMessage());
		}
	}

	protected void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
		logger.info("Message sent succesfully for the key : {} and the value : {},partition is {}", key, value,
				result.getRecordMetadata().partition());

	}

}
