package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityPermissionRepository;
import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.exc.InsufficientAccessToChildrenException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FSEntityPermissionServiceImpl implements FSEntityPermissionService {
	private final FSEntityPermissionRepository repository;
	private final FSEntityRepository entityRepository;

	public FSEntityPermissionServiceImpl(FSEntityPermissionRepository repository, FSEntityRepository entityRepository) {
		this.repository = repository;
		this.entityRepository = entityRepository;
	}

	@Override
	public Optional<FSEntityPermission> getById(Long id) {
		return Optional.empty();
	}

	@Override
	public List<FSEntityPermission> getByFSEntity(FSEntity entity) {
		return null;
	}

	@Override
	public FSEntityPermission create(List<FSEntity> entityList, List<User> users, List<UserRole> userRoles, boolean allow, boolean applicationDefined, List<ActionType> actionTypes) {
		return null;
	}

	@Override
	public FSEntityPermission edit(FSEntityPermission fsEntityPermission, List<FSEntity> entityList, List<User> users, List<UserRole> userRoles, boolean allow, boolean applicationDefined, List<ActionType> actionTypes) {
		return null;
	}

	@Override
	public Optional<FSEntityPermission> check(FSEntity fsEntity, ActionType type, User user) {
		return Optional.empty();
	}

	@Override
	public List<FSEntity> getAllChildrenWithAccess(FSEntity parent, User user, Boolean file, ActionType actionType) {
		if (!repository.havePermissionToAllChildren(parent.getPath(), user.getId(), user.getUserRole(), actionType.allMasks)) {
			throw new InsufficientAccessToChildrenException();
		}
		return entityRepository.findChildrenByPath(parent.getPath());
	}

	@Override
	public void delete(FSEntityPermission permission) {

	}

	@Override
	public void delete(List<FSEntityPermission> list) {

	}

	@Override
	public void deletePermissionsFor(FSEntity parent) {

	}

	@Override
	public void deletePermissionsTreeFor(FSEntity parent) {

	}
}