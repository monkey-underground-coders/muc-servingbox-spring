package com.a6raywa1cher.mucservingboxspring.rest.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import static com.a6raywa1cher.mucservingboxspring.rest.FSEntityController.FS_NAME_REGEXP;

@Data
public class MoveEntityRequest {
	@NotNull
	@Positive
	private Long objectId;

	@NotNull
	@Positive
	private Long targetParentId;

	@NotBlank
	@Pattern(regexp = FS_NAME_REGEXP)
	private String entityName;
}
