package com.a6raywa1cher.mucservingboxspring.rest.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "The folder FSEnitiy is full, can't upload more")
public class ExceededMaximumSpaceCapacityException extends RuntimeException {
}
