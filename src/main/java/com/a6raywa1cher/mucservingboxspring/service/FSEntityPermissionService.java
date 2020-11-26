package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FSEntityPermissionService {
	Optional<FSEntityPermission> getById(Long id);

	List<FSEntityPermission> getByFSEntity(FSEntity fsEntity);

	FSEntityPermission create(FSEntity entity, List<User> users, List<UserRole> userRoles,
							  boolean applicationDefined, List<ActionType> actionTypes);

	FSEntityPermission edit(FSEntityPermission fsEntityPermission, FSEntity entity,
							List<User> users, List<UserRole> userRoles, boolean applicationDefined,
							List<ActionType> actionTypes);

	boolean check(FSEntity fsEntity, ActionType type, User user);

	Map<ActionType, Boolean> probe(FSEntity entity, User user);

	List<FSEntity> getAllChildrenWithAccess(FSEntity parent, User user, ActionType actionType);

	List<FSEntity> getAllDescendantsWithAccess(FSEntity entity, User user, ActionType actionType);

	List<FSEntity> getAllReadable(User user);

	void delete(FSEntityPermission permission);

	void delete(List<FSEntityPermission> list);

	void deletePermissionsTreeFor(FSEntity parent);

}
