package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class EditLessonSchemaRequest {
	@NotBlank
	@Size(max = 120)
	private String title;

	@Size(max = 5120)
	private String description;
}
