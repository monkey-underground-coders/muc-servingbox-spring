package com.a6raywa1cher.mucservingboxspring.rest.exc;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "You're trying to upload an empty file")
public class EmptyFileException extends RuntimeException {
}
