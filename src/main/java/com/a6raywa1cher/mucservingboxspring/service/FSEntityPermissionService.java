package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;

import java.util.List;
import java.util.Optional;

public interface FSEntityPermissionService {
	Optional<FSEntityPermission> getById(Long id);

	List<FSEntityPermission> getByFSEntity(FSEntity entity);

	FSEntityPermission create(List<FSEntity> entityList, List<User> users, List<UserRole> userRoles,
							  boolean allow, boolean applicationDefined, List<ActionType> actionTypes);

	FSEntityPermission edit(FSEntityPermission fsEntityPermission, List<FSEntity> entityList,
							List<User> users, List<UserRole> userRoles, boolean allow, boolean applicationDefined,
							List<ActionType> actionTypes);

	Optional<FSEntityPermission> check(FSEntity fsEntity, ActionType type, User user);

	List<FSEntity> getAllChildrenWithAccess(FSEntity parent, User user, Boolean file, ActionType actionType);

	void delete(FSEntityPermission permission);

	void delete(List<FSEntityPermission> list);

	void deletePermissionsFor(FSEntity parent);

	void deletePermissionsTreeFor(FSEntity parent);
}
