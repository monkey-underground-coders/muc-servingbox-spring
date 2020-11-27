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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
	public void checkSchemaReadAccessUserIsAdmin() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LiveLesson liveLesson = LiveLesson.builder().build();
		LessonSchema lessonSchema = LessonSchema.builder()
			.liveLessons(List.of(liveLesson)).build();
		User adminUser = User.builder().userRole(UserRole.ADMIN).build();

		when(schemaService.getById(1L)).thenReturn(Optional.of(lessonSchema));

		assertTrue(checker.checkSchemaReadAccess(1L, adminUser));
	}

	@Test
	public void checkSchemaReadAccessNotAdminButLessonStatusIsLive() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LiveLesson liveLesson = LiveLesson.builder()
			.startAt(ZonedDateTime.now().minus(10L, ChronoUnit.MINUTES))
			.endAt(ZonedDateTime.now().plus(20L, ChronoUnit.MINUTES)).build();
		LessonSchema lessonSchema = LessonSchema.builder()
			.liveLessons(List.of(liveLesson)).build();
		User notAdminUser = User.builder().userRole(UserRole.TEMPORARY_USER).build();

		when(schemaService.getById(1L)).thenReturn(Optional.of(lessonSchema));

		assertTrue(checker.checkSchemaReadAccess(1L, notAdminUser));
	}

	@Test
	public void checkSchemaReadAccessNotAdminNotLiveLessonStatus() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		LiveLesson liveLesson = LiveLesson.builder()
			.startAt(ZonedDateTime.now().plus(10L, ChronoUnit.MINUTES))
			.endAt(ZonedDateTime.now().plus(20L, ChronoUnit.MINUTES)).build();
		LessonSchema lessonSchema = LessonSchema.builder()
			.liveLessons(List.of(liveLesson)).build();
		User notAdminUser = User.builder().userRole(UserRole.TEMPORARY_USER).build();

		when(schemaService.getById(1L)).thenReturn(Optional.of(lessonSchema));

		assertFalse(checker.checkSchemaReadAccess(1L, notAdminUser));
	}

	@Test
	public void checkSchemaWriteAccessUserIsAdmin() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);


		User notAdminUser = User.builder().userRole(UserRole.ADMIN).build();
		LessonSchema lessonSchema = LessonSchema.builder().build();

		when(schemaService.getById(1L)).thenReturn(Optional.of(lessonSchema));

		assertTrue(checker.checkSchemaWriteAccess(1L, notAdminUser));
	}

	@Test
	public void checkSchemaWriteAccessNotAdminButCreator() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);


		User notAdminUser = User.builder().userRole(UserRole.TEMPORARY_USER).build();
		LessonSchema lessonSchema = LessonSchema.builder().creator(notAdminUser).build();

		when(schemaService.getById(1L)).thenReturn(Optional.of(lessonSchema));

		assertTrue(checker.checkSchemaWriteAccess(1L, notAdminUser));
	}

	@Test
	public void checkSchemaWriteAccessNotAdminNotCreator() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);


		User notAdminUser = User.builder().userRole(UserRole.TEMPORARY_USER).build();
		User anotherUser = User.builder().build();
		LessonSchema lessonSchema = LessonSchema.builder().creator(anotherUser).build();

		when(schemaService.getById(1L)).thenReturn(Optional.of(lessonSchema));

		assertFalse(checker.checkSchemaWriteAccess(1L, notAdminUser));
	}

	@Test
	public void checkLiveLessonAccessUserIsAdmin() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User adminUser = User.builder().userRole(UserRole.ADMIN).build();
		LiveLesson liveLesson = LiveLesson.builder().creator(adminUser).build();

		when(liveLessonService.getById(1L)).thenReturn(Optional.of(liveLesson));

		assertTrue(checker.checkLiveLessonAccess(1L, adminUser));
	}

	@Test
	public void checkLiveLessonAccessNotAdminButCreator() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User notAdminUser = User.builder().userRole(UserRole.TEMPORARY_USER).build();
		LiveLesson liveLesson = LiveLesson.builder().creator(notAdminUser).build();

		when(liveLessonService.getById(1L)).thenReturn(Optional.of(liveLesson));

		assertTrue(checker.checkLiveLessonAccess(1L, notAdminUser));
	}

	@Test
	public void checkLiveLessonAccessNotAdminNotCreator() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);


		User anotherUser = User.builder().build();
		LiveLesson liveLesson = LiveLesson.builder().creator(anotherUser).build();
		User notAdminUser = User.builder().userRole(UserRole.TEMPORARY_USER).build();

		when(liveLessonService.getById(1L)).thenReturn(Optional.of(liveLesson));

		assertFalse(checker.checkLiveLessonAccess(1L, notAdminUser));
	}

	@Test
	public void checkUserInternalInfoAccessUserNotAdminButEqualsToNeededID() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(1L).userRole(UserRole.TEMPORARY_USER).build();

		assertFalse(checker.checkUserInternalInfoAccess(1L, requester));
	}

	@Test
	public void checkUserInternalInfoAccessUserIsAdminAndNotEqualsToNeededID() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(2L).userRole(UserRole.ADMIN).build();

		assertFalse(checker.checkUserInternalInfoAccess(1L, requester));
	}

	@Test
	public void checkUserInternalInfoAccessUserNotAdminAndNotEqualsToNeededID() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(2L).userRole(UserRole.TEMPORARY_USER).build();

		assertFalse(checker.checkUserInternalInfoAccess(1L, requester));
	}

	@Test
	public void checkUserPasswordChangeAccessUserIsTemporaryUser() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(1L).userRole(UserRole.TEMPORARY_USER).build();
		User targetUser = User.builder().id(2L).userRole(UserRole.TEMPORARY_USER).build();

		when(userService.getById(2L)).thenReturn(Optional.of(targetUser));


		assertFalse(checker.checkUserPasswordChangeAccess(2L, requester));
	}

	@Test
	public void checkUserPasswordChangeAccessUserIsStudentAndHasEqualsID() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(2L).userRole(UserRole.STUDENT).build();
		User targetUser = User.builder().id(2L).userRole(UserRole.STUDENT).build();

		when(userService.getById(2L)).thenReturn(Optional.of(targetUser));


		assertTrue(checker.checkUserPasswordChangeAccess(2L, requester));
	}

	@Test
	public void checkUserPasswordChangeAccessUserIsStudentAndDoesntHaveEqualsID() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(1L).userRole(UserRole.STUDENT).build();
		User targetUser = User.builder().id(2L).userRole(UserRole.STUDENT).build();

		when(userService.getById(2L)).thenReturn(Optional.of(targetUser));


		assertFalse(checker.checkUserPasswordChangeAccess(2L, requester));
	}

	@Test
	public void checkUserPasswordChangeAccessUserIsTeacherAndHasEqualsID() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(2L).userRole(UserRole.TEACHER).build();
		User targetUser = User.builder().id(2L).userRole(UserRole.TEACHER).build();

		when(userService.getById(2L)).thenReturn(Optional.of(targetUser));


		assertTrue(checker.checkUserPasswordChangeAccess(2L, requester));
	}

	@Test
	public void checkUserPasswordChangeAccessUserIsTeacherAndDoesntHaveEqualsID() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(1L).userRole(UserRole.TEACHER).build();
		User targetUser = User.builder().id(2L).userRole(UserRole.TEACHER).build();

		when(userService.getById(2L)).thenReturn(Optional.of(targetUser));


		assertFalse(checker.checkUserPasswordChangeAccess(2L, requester));
	}

	@Test
	public void checkUserPasswordChangeAccessUserIsAdmin() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		User requester = User.builder().id(1L).userRole(UserRole.ADMIN).build();
		User targetUser = User.builder().id(2L).userRole(UserRole.TEACHER).build();

		when(userService.getById(2L)).thenReturn(Optional.of(targetUser));


		assertTrue(checker.checkUserPasswordChangeAccess(2L, requester));
	}
}


