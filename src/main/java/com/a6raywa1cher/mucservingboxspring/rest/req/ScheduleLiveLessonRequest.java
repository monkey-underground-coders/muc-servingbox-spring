package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.ZonedDateTime;

@Data
public class ScheduleLiveLessonRequest {
	@NotNull
	@Positive
	private Long schemaId;

	@NotBlank
	@Size(max = 255)
	private String name;

	@NotNull
	@Future
	private ZonedDateTime start;

	@NotNull
	@Future
	private ZonedDateTime end;
}
