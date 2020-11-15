package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangeHomeSizeRequest {
	@NotNull
	private Long newSize;
}
