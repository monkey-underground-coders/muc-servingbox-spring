package com.a6raywa1cher.mucservingboxspring.rest.req;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class CreatePermissionRequest {
	@NotNull
	@PositiveOrZero
	private Long entityId;

	@NotNull
	private List<Long> userIds;

	@NotNull
	private List<UserRole> userRoles;

	@NotNull
	@Size(min = 1)
	private List<ActionType> actionTypes;
}
