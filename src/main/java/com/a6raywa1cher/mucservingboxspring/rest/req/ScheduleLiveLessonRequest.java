package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;

@Data
public class ScheduleLiveLessonRequest {
	@NotNull
	@Positive
	private Long schemaId;

	@NotNull
	@Future
	private ZonedDateTime start;

	@NotNull
	@Future
	private ZonedDateTime end;
}
