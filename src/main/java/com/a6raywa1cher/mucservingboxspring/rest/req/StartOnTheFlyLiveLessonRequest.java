package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Data
public class StartOnTheFlyLiveLessonRequest {
	@NotBlank
	@Size(max = 255)
	private String name;

	@NotNull
	@Future
	private ZonedDateTime end;
}
