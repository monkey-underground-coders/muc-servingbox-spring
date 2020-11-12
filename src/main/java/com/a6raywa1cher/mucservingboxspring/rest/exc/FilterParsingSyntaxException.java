package com.a6raywa1cher.mucservingboxspring.rest.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Syntax error on the filter parsing")
public class FilterParsingSyntaxException extends RuntimeException {
	public FilterParsingSyntaxException(Throwable cause) {
		super(cause);
	}
}
