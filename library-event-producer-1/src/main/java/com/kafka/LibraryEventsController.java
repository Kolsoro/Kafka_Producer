package com.kafka;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
public class LibraryEventsController {
	private static final Logger logger=LoggerFactory.getLogger(LibraryEventsController.class);
	 
	@Autowired
	LibraryEventProducer libraryEventProducer;

	@PostMapping("/v1/libraryevent")
	public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
			throws Exception {

		libraryEvent.setLibraryEventType(LibraryEventType.NEW);
		libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent);
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
	}

	@PutMapping("/v1/libraryevent")
	public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent)
			throws Exception {

		if(libraryEvent.getLibraryEventId()==null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the library event id");
		}
		libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
		libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent);
		return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
	}

}
