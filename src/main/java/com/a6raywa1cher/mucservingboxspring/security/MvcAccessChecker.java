package com.a6raywa1cher.mucservingboxspring.security;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLessonStatus;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.LiveLessonService;
import com.a6raywa1cher.mucservingboxspring.utils.AuthenticationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
@Slf4j
@Transactional
public class MvcAccessChecker {
	private final FSEntityService fsEntityService;
	private final FSEntityPermissionService permissionService;
	private final AuthenticationResolver resolver;
	private final LessonSchemaService schemaService;
	private final LiveLessonService liveLessonService;

	public MvcAccessChecker(FSEntityService fsEntityService, FSEntityPermissionService permissionService,
							AuthenticationResolver resolver, LessonSchemaService schemaService,
							LiveLessonService liveLessonService) {
		this.fsEntityService = fsEntityService;
		this.permissionService = permissionService;
		this.resolver = resolver;
		this.schemaService = schemaService;
		this.liveLessonService = liveLessonService;
	}

	// ----------------------------------------------- checkEntityAccess -----------------------------------------------

	private boolean checkEntityAccess(FSEntity fsEntity, ActionType actionType, User user) {
		if (fsEntity.isFile() || actionType == ActionType.READ) {
			return permissionService.check(fsEntity, actionType, user);
		} else {
			return fsEntityService.getParent(fsEntity)
				.map(p -> permissionService.check(p, actionType, user))
				.orElse(false);
		}
	}

	public boolean checkEntityAccessById(Long id, ActionType actionType, User user) {
		Optional<FSEntity> optionalFSEntity = fsEntityService.getById(id);
		if (optionalFSEntity.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity fsEntity = optionalFSEntity.get();
		return checkEntityAccess(fsEntity, actionType, user);
	}

	public boolean checkEntityAccessByPath(String path, ActionType actionType, User user) {
		Optional<FSEntity> optionalFSEntity = fsEntityService.getByPath(path);
		if (optionalFSEntity.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity fsEntity = optionalFSEntity.get();
		return checkEntityAccess(fsEntity, actionType, user);
	}

	public boolean checkEntityAccessById(Long id, String actionType) {
		return checkEntityAccessById(id, ActionType.resolve(actionType), getCurrentUser());
	}

	public boolean checkEntityAccessByPath(String path, String actionType) {
		return checkEntityAccessByPath(path, ActionType.resolve(actionType), getCurrentUser());
	}

	// ------------------------------------------------ checkLowerAccess -----------------------------------------------

	private boolean checkLowerAccess(FSEntity parent, ActionType actionType, User user) {
		return permissionService.check(parent, actionType, user);
	}

	public boolean checkLowerAccessById(Long parentId, ActionType actionType, User user) {
		Optional<FSEntity> optionalFSEntity = fsEntityService.getById(parentId);
		if (optionalFSEntity.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity fsEntity = optionalFSEntity.get();
		return checkLowerAccess(fsEntity, actionType, user);
	}

	public boolean checkLowerAccessById(Long parentId, String actionType) {
		return checkLowerAccessById(parentId, ActionType.resolve(actionType), getCurrentUser());
	}

	// --------------------------------------------- checkPermissionAccess ---------------------------------------------

	public boolean checkPermissionAccess(Long id, User user) {
		Optional<FSEntityPermission> entityPermission = permissionService.getById(id);
		if (entityPermission.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		FSEntity entity = entityPermission.get().getEntity();
		return this.checkEntityAccess(entity, ActionType.MANAGE_PERMISSIONS, user);
	}

	public boolean checkPermissionAccess(Long id) {
		return this.checkPermissionAccess(id, getCurrentUser());
	}

	// --------------------------------------------- checkSchemaReadAccess ---------------------------------------------

	public boolean checkSchemaReadAccess(Long id, User user) {
		Optional<LessonSchema> optionalLessonSchema = schemaService.getById(id);
		if (optionalLessonSchema.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		LessonSchema lessonSchema = optionalLessonSchema.get();
		if (user.getUserRole() == UserRole.ADMIN || user.equals(lessonSchema.getCreator())) {
			return true;
		}
		return lessonSchema.getLiveLessons().stream().anyMatch(l -> l.getStatus() == LiveLessonStatus.LIVE);
	}

	public boolean checkSchemaReadAccess(Long id) {
		return this.checkSchemaReadAccess(id, getCurrentUser());
	}

	// --------------------------------------------- checkSchemaWriteAccess --------------------------------------------

	public boolean checkSchemaWriteAccess(Long id, User user) {
		Optional<LessonSchema> optionalLessonSchema = schemaService.getById(id);
		if (optionalLessonSchema.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		LessonSchema lessonSchema = optionalLessonSchema.get();
		return user.getUserRole() == UserRole.ADMIN || lessonSchema.getCreator().equals(user);
	}

	public boolean checkSchemaWriteAccess(Long id) {
		return this.checkSchemaWriteAccess(id, getCurrentUser());
	}

	// --------------------------------------------- checkLiveLessonAccess ---------------------------------------------

	public boolean checkLiveLessonAccess(Long id, User user) {
		Optional<LiveLesson> optionalLiveLesson = liveLessonService.getById(id);
		if (optionalLiveLesson.isEmpty()) {
			return true; // 404 error will be thrown by the controller
		}
		LiveLesson liveLesson = optionalLiveLesson.get();
		return user.getUserRole() == UserRole.ADMIN || liveLesson.getCreator().equals(user);
	}

	public boolean checkLiveLessonAccess(Long id) {
		return this.checkLiveLessonAccess(id, getCurrentUser());
	}

	private User getCurrentUser() {
		return resolver.getUser();
	}
}
