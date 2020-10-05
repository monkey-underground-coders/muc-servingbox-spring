package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChangeNameOfUserRequest {
	@NotBlank
	@Size(max = 50)
	private String name;
}
