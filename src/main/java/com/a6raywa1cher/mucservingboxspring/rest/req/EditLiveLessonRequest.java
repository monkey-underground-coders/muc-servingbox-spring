package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Data
public class EditLiveLessonRequest {
	@NotBlank
	@Size(max = 255)
	private String name;

	private ZonedDateTime start;

	@FutureOrPresent
	private ZonedDateTime end;
}
