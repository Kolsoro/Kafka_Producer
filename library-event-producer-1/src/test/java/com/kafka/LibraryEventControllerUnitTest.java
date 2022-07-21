package com.kafka;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {

	@Autowired
	MockMvc mockMvc;
	
	ObjectMapper objectMapper=new ObjectMapper();
	
	@MockBean
	LibraryEventProducer libraryEventProducer;
	

	@Test
	void postLibraryEvent() throws Exception {
		
         //given
		
		Book book = new Book(123, "spring with kafka", "kamal");
		LibraryEvent libraryEvent = new LibraryEvent(null, book, LibraryEventType.NEW);
		
		String json=objectMapper.writeValueAsString(libraryEvent);
		
		when(libraryEventProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

		//when
		
		mockMvc.perform(post("/v1/libraryevent")
				.content(json) 
				.contentType(MediaType.APPLICATION_JSON))
		        .andExpect(status().isCreated());
		
		//then 
		
	}
	
	@Test
	void postLibraryEvent_4xx() throws Exception {
		
         //given		
		Book book = new Book(null, "spring with kafka", null);

		LibraryEvent libraryEvent = new LibraryEvent(null, book, LibraryEventType.NEW);
		
		String json=objectMapper.writeValueAsString(libraryEvent);
		
		when(libraryEventProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);
		 
		//expect
		
		String expectedErrorMessage ="book.bookAuthor  -   must not be blank, book.bookId  -   must not be null";
		
		mockMvc.perform(post("/v1/libraryevent")
				.content(json) 
				.contentType(MediaType.APPLICATION_JSON))
		        .andExpect(status().is4xxClientError())
		        .andExpect(content().string(expectedErrorMessage));
		
		
		
	}

}
