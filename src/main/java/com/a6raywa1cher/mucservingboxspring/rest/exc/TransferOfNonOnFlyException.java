package com.a6raywa1cher.mucservingboxspring.rest.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Can't transfer non-on-fly LessonSchema")
public class TransferOfNonOnFlyException extends RuntimeException {
}
