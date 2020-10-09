package com.a6raywa1cher.mucservingboxspring.service.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE, reason = "The server is processing this FSEntity")
public class ResourceBusyException extends RuntimeException {
}
