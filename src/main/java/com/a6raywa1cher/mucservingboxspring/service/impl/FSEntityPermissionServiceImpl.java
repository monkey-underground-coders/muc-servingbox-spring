package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityPermissionRepository;
import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.utils.AlgorithmUtils;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
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
	public List<FSEntityPermission> getByFSEntity(FSEntity fsEntity) {
		return repository.getByEntity(fsEntity);
	}

	@Override
	public FSEntityPermission create(FSEntity entity, List<User> users, List<UserRole> userRoles, boolean applicationDefined, List<ActionType> actionTypes) {
		return edit(new FSEntityPermission(), entity, users, userRoles, applicationDefined, actionTypes, ZonedDateTime.now(), ZonedDateTime.now().plus(100, ChronoUnit.YEARS));
	}

	@Override
	public FSEntityPermission create(FSEntity entity, List<User> users, List<UserRole> userRoles, boolean applicationDefined, List<ActionType> actionTypes, ZonedDateTime start, ZonedDateTime end) {
		return edit(new FSEntityPermission(), entity, users, userRoles, applicationDefined, actionTypes, start, end);
	}

	@Override
	public FSEntityPermission edit(FSEntityPermission fsEntityPermission, FSEntity entity, List<User> users, List<UserRole> userRoles, boolean applicationDefined, List<ActionType> actionTypes) {
		return edit(fsEntityPermission, entity, users, userRoles, applicationDefined, actionTypes, fsEntityPermission.getStartAt(), fsEntityPermission.getEndAt());
	}

	@Override
	public FSEntityPermission edit(FSEntityPermission fsEntityPermission, FSEntity entity, List<User> users, List<UserRole> userRoles, boolean applicationDefined, List<ActionType> actionTypes, ZonedDateTime start, ZonedDateTime end) {
		fsEntityPermission.setEntity(entity);
		fsEntityPermission.setAffectedUserRoles(new ArrayList<>(userRoles));
		fsEntityPermission.setAffectedUsers(new ArrayList<>(users));
		fsEntityPermission.setApplicationDefined(applicationDefined);
		fsEntityPermission.setActionTypes(actionTypes);
		fsEntityPermission.setStartAt(start);
		fsEntityPermission.setEndAt(end);
		return repository.save(fsEntityPermission);
	}


	@Override
	public boolean check(FSEntity fsEntity, ActionType type, User user) {
		if (user.getUserRole().access.contains(fsEntity.getCreatedBy().getUserRole())) {
			return true;
		}
		return repository.checkAccess(getUpperLevels(fsEntity.getPath()), user.getId(),
			user.getUserRole(), type.allMasks, ZonedDateTime.now());
	}

	@Override
	public Map<ActionType, Boolean> probe(FSEntity entity, User user) {
		List<FSEntityPermission> permissions =
			repository.getAllActiveApplicableToEntity(getUpperLevels(entity.getPath()), user.getId(), user.getUserRole(), ZonedDateTime.now());
		Map<ActionType, Boolean> out = permissions.stream()
			.flatMap(p -> p.getActionTypes().stream())
			.distinct()
			.collect(Collectors.toMap(Function.identity(), a -> true));
		out.putIfAbsent(ActionType.READ, false);
		out.putIfAbsent(ActionType.WRITE, false);
		out.putIfAbsent(ActionType.MANAGE_PERMISSIONS, false);
		return out;
	}

	@Override
	public List<FSEntity> getAllChildrenWithAccess(FSEntity parent, User user, ActionType actionType) {
		List<FSEntity> treeByPath = entityRepository.getTreeByPath(parent.getPath());
		return treeByPath.stream().filter(e -> !parent.getId().equals(e.getId())).collect(Collectors.toList());
	}

	@Override
	public List<FSEntity> getAllDescendantsWithAccess(FSEntity entity, User user, ActionType actionType) {
		return entityRepository.getFirstLevelByPath(entity.getPath(), entity.getPathLevel() + 1);
	}

	private List<String> getAllPath(String s) {
		List<Integer> slashes = AlgorithmUtils.getSlashes(s);
		List<String> out = new ArrayList<>(slashes.size());
		out.add("/");
		for (int i = 0; i < slashes.size(); i++) {
			String node;
			if (i + 1 == slashes.size()) {
				node = s;
			} else {
				int right = slashes.get(i + 1);
				node = s.substring(0, right + 1);
			}
			if (node.equals("")) continue;
			out.add(node);
		}
		return out;
	}

	@Override
	public List<FSEntity> getAllReadable(User user) {
		List<FSEntity> entities =
			repository.getActiveByUserAndMasks(user.getUserRole(), user.getId(), ActionType.READ.allMasks, ZonedDateTime.now())
				.stream()
				.map(FSEntityPermission::getEntity)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		Map<String, FSEntity> pathToEntity = entities.stream()
			.collect(Collectors.toMap(FSEntity::getPath, Function.identity()));
		Map<String, Set<String>> graph = new HashMap<>();
		for (FSEntity e : entities) {
			List<String> allPath = getAllPath(e.getPath());
			for (int i = 1; i < allPath.size(); i++) {
				String n1 = allPath.get(i - 1);
				String n2 = allPath.get(i);
				if (!graph.containsKey(n1)) {
					graph.put(n1, new HashSet<>());
				}
				graph.get(n1).add(n2);
			}
			String lastNode = allPath.get(allPath.size() - 1);
			graph.putIfAbsent(lastNode, new HashSet<>());
		}

		List<FSEntity> out = new ArrayList<>();
		Queue<String> queue = new ArrayDeque<>();
		Set<String> visited = new HashSet<>();
		queue.add("/");
		visited.add("/");
		while (queue.size() > 0) {
			String node = queue.poll();
			FSEntity entity;
			if ((entity = pathToEntity.getOrDefault(node, null)) != null) {
				out.add(entity);
				continue;
			}
			for (String child : graph.get(node)) {
				if (!visited.contains(child)) {
					queue.add(child);
					visited.add(child);
				}
			}
		}
		return out;
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
