package com.a6raywa1cher.mucservingboxspring.security;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.utils.AuthenticationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class MvcAccessChecker {
	private final FSEntityService fsEntityService;
	private final FSEntityPermissionService permissionService;
	private final AuthenticationResolver resolver;

	public MvcAccessChecker(FSEntityService fsEntityService, FSEntityPermissionService permissionService,
							AuthenticationResolver resolver) {
		this.fsEntityService = fsEntityService;
		this.permissionService = permissionService;
		this.resolver = resolver;
	}

	// ------------------------------------------------ checkEntityAccess ------------------------------------------------

	private boolean checkEntityAccess(FSEntity fsEntity, ActionType actionType, User user) {
		if (fsEntity.isFile() || actionType == ActionType.READ) {
			return permissionService.check(fsEntity, actionType, user);
		} else {
			return fsEntityService.getParent(fsEntity)
				.map(p -> permissionService.check(p, actionType, user))
				.orElse(false);
		}
	}

	public boolean checkEntityAccessById(Long id, ActionType actionType, Authentication authentication) {
		User user = resolver.getUser(authentication);
		Optional<FSEntity> optionalFSEntity = fsEntityService.getById(id);
		if (optionalFSEntity.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity fsEntity = optionalFSEntity.get();
		return checkEntityAccess(fsEntity, actionType, user);
	}

	public boolean checkEntityAccessByPath(String path, ActionType actionType, Authentication authentication) {
		User user = resolver.getUser(authentication);
		Optional<FSEntity> optionalFSEntity = fsEntityService.getByPath(path);
		if (optionalFSEntity.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity fsEntity = optionalFSEntity.get();
		return checkEntityAccess(fsEntity, actionType, user);
	}

	public boolean checkEntityAccessById(Long id, String actionType) {
		return checkEntityAccessById(id, ActionType.resolve(actionType), getAuthentication());
	}

	public boolean checkEntityAccessByPath(String path, String actionType) {
		return checkEntityAccessByPath(path, ActionType.resolve(actionType), getAuthentication());
	}

	// ------------------------------------------------ checkLowerAccess ------------------------------------------------

	private boolean checkLowerAccess(FSEntity parent, ActionType actionType, User user) {
		return permissionService.check(parent, actionType, user);
	}

	public boolean checkLowerAccessById(Long parentId, ActionType actionType, Authentication authentication) {
		User user = resolver.getUser(authentication);
		Optional<FSEntity> optionalFSEntity = fsEntityService.getById(parentId);
		if (optionalFSEntity.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity fsEntity = optionalFSEntity.get();
		return checkLowerAccess(fsEntity, actionType, user);
	}

	public boolean checkLowerAccessById(Long parentId, String actionType) {
		return checkLowerAccessById(parentId, ActionType.resolve(actionType), getAuthentication());
	}

	// ------------------------------------------------ checkPermissionEditAccess ------------------------------------------------

	public boolean checkPermissionAccess(Long id, Authentication authentication) {
		User user = resolver.getUser(authentication);
		Optional<FSEntityPermission> entityPermission = permissionService.getById(id);
		if (entityPermission.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity entity = entityPermission.get().getEntity();
		return this.checkEntityAccess(entity, ActionType.MANAGE_PERMISSIONS, user);
	}

	private Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
}
