package com.a6raywa1cher.mucservingboxspring.rest.req;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import lombok.Data;

import java.util.List;

@Data
public class CreatePermissionRequest {
	private List<Long> entityIds;

	private List<Long> userIds;

	private List<UserRole> userRoles;

	private List<ActionType> actionTypes;
}
