package com.a6raywa1cher.mucservingboxspring.unit;


import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLessonStatus;
import com.a6raywa1cher.mucservingboxspring.security.MvcAccessChecker;
import com.a6raywa1cher.mucservingboxspring.service.*;
import com.a6raywa1cher.mucservingboxspring.utils.AuthenticationResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MvcAccessCheckerUnitTests {
	@Mock
	private FSEntityService fsEntityService;
	@Mock
	private FSEntityPermissionService permissionService;
	@Mock
	private AuthenticationResolver resolver;
	@Mock
	private LessonSchemaService schemaService;
	@Mock
	private LiveLessonService liveLessonService;
	@Mock
	private UserService userService;


	private void setPermissions(User user, FSEntity targetFolder, boolean read, boolean write, boolean permissionAccess){
		when(permissionService.check(targetFolder, ActionType.READ, user)).thenReturn(read);
		when(permissionService.check(targetFolder, ActionType.WRITE, user)).thenReturn(write);
		when(permissionService.check(targetFolder, ActionType.MANAGE_PERMISSIONS, user)).thenReturn(permissionAccess);
	}

	private void checkPermissions(Function<ActionType, Boolean> function, boolean read, boolean write, boolean permissionAccess){
		assertEquals(function.apply(ActionType.READ), read);
		assertEquals(function.apply(ActionType.WRITE), write);
		assertEquals(function.apply(ActionType.MANAGE_PERMISSIONS), permissionAccess);
	}

	@Test
	public void checkLowerAccessById() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		FSEntity targetFolder = FSEntity.createFolder("/f1/snake/", mock(User.class), false, 100L);
		FSEntity parentFolder = FSEntity.createFolder("/f1/", mock(User.class), false, 100L);
		User user = mock(User.class, "user");

		when(fsEntityService.getById(14L)).thenReturn(Optional.of(targetFolder));
		when(fsEntityService.getById(15L)).thenReturn(Optional.of(parentFolder));
		setPermissions(user, targetFolder, true, false, false);
		setPermissions(user, parentFolder, true, true, false);

		checkPermissions((actionType) -> checker.checkLowerAccessById(14L, actionType, user), true, false, false);
		checkPermissions((actionType) -> checker.checkLowerAccessById(15L, actionType, user), true, true, false);
	}

	@Test
	public void checkFileAccessPath() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		FSEntity targetFile = FSEntity.createFile("/f1/meow", "/134", 25L,
			mock(User.class), false);
		User user1 = mock(User.class, "user1");
		User user2 = mock(User.class, "user2");

		when(fsEntityService.getById(14L)).thenReturn(Optional.of(targetFile));
		setPermissions(user1, targetFile, true, false, false);
		setPermissions(user2, targetFile, false, true, false);

		checkPermissions((actionType) -> checker.checkEntityAccessById(14L, actionType, user1), true, false, false);
		checkPermissions((actionType) -> checker.checkEntityAccessById(14L, actionType, user2), false, true, false);
	}

	@Test
	public void checkFolderAccessPath() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		FSEntity targetFolder = FSEntity.createFolder("/f1/f2/", mock(User.class), false);
		FSEntity parentFolder = FSEntity.createFolder("/f1/", mock(User.class), false);
		User user = mock(User.class, "user");

		when(fsEntityService.getById(14L)).thenReturn(Optional.of(targetFolder));
		when(fsEntityService.getById(15L)).thenReturn(Optional.of(parentFolder));
		when(fsEntityService.getParent(targetFolder)).thenReturn(Optional.of(parentFolder));
		setPermissions(user, targetFolder, true, true, true);
		setPermissions(user, parentFolder, true, false, true);

		checkPermissions((actionType) -> checker.checkEntityAccessById(14L, actionType, user), true, false, true);
		checkPermissions((actionType) -> checker.checkEntityAccessById(15L, actionType, user), true, false, false);
	}

	@Test
	public void checkPermissionAccess() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		FSEntity targetFolder = FSEntity.createFolder("/f1/f2/", mock(User.class), false);
		FSEntity parentFolder = FSEntity.createFolder("/f1/", mock(User.class), false);
		FSEntityPermission fsEntityPermission = FSEntityPermission.builder()
			.entity(targetFolder)
			.build();
		User user = mock(User.class, "user");

		when(fsEntityService.getParent(targetFolder)).thenReturn(Optional.of(parentFolder));
		when(permissionService.check(parentFolder, ActionType.MANAGE_PERMISSIONS, user)).thenReturn(true);
		when(permissionService.getById(16L)).thenReturn(Optional.of(fsEntityPermission));

		assertTrue(checker.checkPermissionAccess(16L, user));
	}

	@Test
	public void checkSchemaReadAccess() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		Optional<LessonSchema> optionalLessonSchema = Optional.of(LessonSchema.builder().build());
		User adminUser = mock(User.class, "admin");

		when(schemaService.getById(1L)).thenReturn(optionalLessonSchema);
		when(adminUser.getUserRole()).thenReturn(UserRole.ADMIN);

		assertTrue(checker.checkSchemaReadAccess(1L, adminUser));
	}

	@Test
	public void checkSchemaReadAccessNotAdmin() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LiveLesson liveLesson = mock(LiveLesson.class);
		LessonSchema lessonSchema = LessonSchema.builder()
			.liveLessons(List.of(liveLesson)).build();
		Optional<LessonSchema> optionalLessonSchema = Optional.of(lessonSchema);
		User notAdminUser = mock(User.class, "notAdmin");

		when(schemaService.getById(1L)).thenReturn(optionalLessonSchema);
		when(notAdminUser.getUserRole()).thenReturn(UserRole.TEMPORARY_USER);
		when(liveLesson.getStatus()).thenReturn(LiveLessonStatus.LIVE);

		assertTrue(checker.checkSchemaReadAccess(1L, notAdminUser));
	}

	@Test
	public void checkSchemaReadAccessNotAdminNotTrue() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LiveLesson liveLesson = mock(LiveLesson.class);
		LessonSchema lessonSchema = LessonSchema.builder()
			.liveLessons(List.of(liveLesson)).build();
		Optional<LessonSchema> optionalLessonSchema = Optional.of(lessonSchema);
		User notAdminUser = mock(User.class, "notAdmin");

		when(schemaService.getById(1L)).thenReturn(optionalLessonSchema);
		when(notAdminUser.getUserRole()).thenReturn(UserRole.TEMPORARY_USER);
		when(liveLesson.getStatus()).thenReturn(LiveLessonStatus.SCHEDULED);

		assertFalse(checker.checkSchemaReadAccess(1L, notAdminUser));
	}

	@Test
	public void checkSchemaWriteAccess() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LessonSchema lessonSchema = LessonSchema.builder().build();
		Optional<LessonSchema> optionalLessonSchema = Optional.of(lessonSchema);
		User adminUser = mock(User.class, "admin");

		when(schemaService.getById(1L)).thenReturn(optionalLessonSchema);
		when(adminUser.getUserRole()).thenReturn(UserRole.ADMIN);

		assertTrue(checker.checkSchemaReadAccess(1L, adminUser));
	}

	@Test
	public void checkSchemaWriteAccessNotAdminButCreator() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LessonSchema lessonSchema = mock(LessonSchema.class);
		Optional<LessonSchema> optionalLessonSchema = Optional.of(lessonSchema);
		User notAdminUser = mock(User.class, "notAdmin");

		when(schemaService.getById(1L)).thenReturn(optionalLessonSchema);
		when(notAdminUser.getUserRole()).thenReturn(UserRole.TEMPORARY_USER);
		when(lessonSchema.getCreator()).thenReturn(notAdminUser);

		assertTrue(checker.checkSchemaReadAccess(1L, notAdminUser));
	}

	@Test
	public void checkSchemaWriteAccessNotAdminNotCreator() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LessonSchema lessonSchema = mock(LessonSchema.class);
		Optional<LessonSchema> optionalLessonSchema = Optional.of(lessonSchema);
		User notAdminUser = mock(User.class, "notAdmin");
		User anotherUser = mock(User.class, "user");

		when(schemaService.getById(1L)).thenReturn(optionalLessonSchema);
		when(notAdminUser.getUserRole()).thenReturn(UserRole.TEMPORARY_USER);
		when(lessonSchema.getCreator()).thenReturn(anotherUser);

		assertFalse(checker.checkSchemaReadAccess(1L, notAdminUser));
	}
}


