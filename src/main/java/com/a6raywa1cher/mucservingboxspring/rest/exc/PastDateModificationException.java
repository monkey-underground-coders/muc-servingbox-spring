package com.a6raywa1cher.mucservingboxspring.rest.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Cannot modify a date in the LiveLesson, which is from the past")
public class PastDateModificationException extends RuntimeException {
}
