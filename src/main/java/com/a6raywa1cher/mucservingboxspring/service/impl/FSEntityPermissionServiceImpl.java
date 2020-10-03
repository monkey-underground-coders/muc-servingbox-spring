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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FSEntityPermissionServiceImpl implements FSEntityPermissionService {
	private final FSEntityPermissionRepository repository;
	private final FSEntityRepository entityRepository;

	public FSEntityPermissionServiceImpl(FSEntityPermissionRepository repository, FSEntityRepository entityRepository) {
		this.repository = repository;
		this.entityRepository = entityRepository;
	}

	private static List<String> getUpperLevels(String path) {
		if (path == null) return new ArrayList<>();
		if (path.equals("/")) return Collections.singletonList(path);
		String finalPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		List<String> out = IntStream.range(0, finalPath.length())
			.filter(i -> finalPath.charAt(i) == '/')
			.boxed()
			.map(i -> finalPath.substring(0, i + 1))
			.collect(Collectors.toList());
		out.add(path);
		return out;
	}

	@Override
	public Optional<FSEntityPermission> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public List<FSEntityPermission> getChildrenByFSEntity(FSEntity entity) {
		return repository.getAllByPath(entity.getPath());
	}

	@Override
	public FSEntityPermission create(List<FSEntity> entityList, List<User> users, List<UserRole> userRoles, boolean applicationDefined, List<ActionType> actionTypes) {
		return edit(new FSEntityPermission(), entityList, users, userRoles, applicationDefined, actionTypes);
	}

	@Override
	public FSEntityPermission edit(FSEntityPermission fsEntityPermission, List<FSEntity> entityList, List<User> users, List<UserRole> userRoles, boolean applicationDefined, List<ActionType> actionTypes) {
		fsEntityPermission.setEntities(new ArrayList<>(entityList));
		fsEntityPermission.setAffectedUserRoles(new ArrayList<>(userRoles));
		fsEntityPermission.setAffectedUsers(new ArrayList<>(users));
		fsEntityPermission.setApplicationDefined(applicationDefined);
		fsEntityPermission.setActionTypes(actionTypes);
		return repository.save(fsEntityPermission);
	}


	@Override
	public boolean check(FSEntity fsEntity, ActionType type, User user) {
		if (user.getUserRole().access.contains(fsEntity.getCreatedBy().getUserRole())) {
			return true;
		}
		return repository.checkAccess(getUpperLevels(fsEntity.getPath()), user.getId(), user.getUserRole(), type.allMasks);
	}

	@Override
	public List<FSEntity> getAllChildrenWithAccess(FSEntity parent, User user, ActionType actionType) {
		if (!repository.checkAccess(getUpperLevels(parent.getPath()), user.getId(), user.getUserRole(), actionType.allMasks)) {
			throw new InsufficientAccessToChildrenException();
		}
		return entityRepository.getTreeByPath(parent.getPath());
	}

	@Override
	public void delete(FSEntityPermission permission) {
		repository.delete(permission);
	}

	@Override
	public void delete(List<FSEntityPermission> list) {
		repository.deleteAll(list);
	}


	@Override
	public void deletePermissionsTreeFor(FSEntity parent) {
		repository.deleteAll(repository.getAllByPath(parent.getPath()));
	}
}
