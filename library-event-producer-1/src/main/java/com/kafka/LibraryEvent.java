package com.kafka;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {

	private Integer libraryEventId;

	@NotNull
	@Valid
	private Book book;

	private LibraryEventType libraryEventType;

	public LibraryEvent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LibraryEvent(Integer libraryEventId, Book book, LibraryEventType libraryEventType) {
		super();
		this.libraryEventId = libraryEventId;
		this.book = book;
		this.libraryEventType = libraryEventType;
	}

	public LibraryEventType getLibraryEventType() {
		return libraryEventType;
	}

	public void setLibraryEventType(LibraryEventType libraryEventType) {
		this.libraryEventType = libraryEventType;
	}

	public Integer getLibraryEventId() {
		return libraryEventId;
	}

	public void setLibraryEventId(Integer libraryEventId) {
		this.libraryEventId = libraryEventId;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	@Override
	public String toString() {
		return "LibraryEvent [libraryEventId=" + libraryEventId + ", book=" + book + ", libraryEventType="
				+ libraryEventType + "]";
	}

}
