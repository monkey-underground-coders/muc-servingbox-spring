package com.a6raywa1cher.mucservingboxspring.unit;


import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.security.MvcAccessChecker;
import com.a6raywa1cher.mucservingboxspring.service.*;
import com.a6raywa1cher.mucservingboxspring.utils.AuthenticationResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

	@Test
	public void checkFileAccessPath() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver, schemaService, liveLessonService, userService);

		FSEntity targetFile = FSEntity.createFile("/f1/meow", "/134", 25L,
			mock(User.class), false);
		User user1 = mock(User.class, "user1");
		User user2 = mock(User.class, "user2");

		when(fsEntityService.getById(14L)).thenReturn(Optional.of(targetFile));
		when(permissionService.check(targetFile, ActionType.READ, user1)).thenReturn(true);
		when(permissionService.check(targetFile, ActionType.WRITE, user1)).thenReturn(false);
		when(permissionService.check(targetFile, ActionType.MANAGE_PERMISSIONS, user1)).thenReturn(false);
		when(permissionService.check(targetFile, ActionType.READ, user2)).thenReturn(false);
		when(permissionService.check(targetFile, ActionType.WRITE, user2)).thenReturn(true);
		when(permissionService.check(targetFile, ActionType.MANAGE_PERMISSIONS, user2)).thenReturn(false);

		assertTrue(checker.checkEntityAccessById(14L, ActionType.READ, user1));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.WRITE, user1));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.MANAGE_PERMISSIONS, user1));

		assertFalse(checker.checkEntityAccessById(14L, ActionType.READ, user2));
		assertTrue(checker.checkEntityAccessById(14L, ActionType.WRITE, user2));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.MANAGE_PERMISSIONS, user2));
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
		when(permissionService.check(targetFolder, ActionType.READ, user)).thenReturn(true);
		when(permissionService.check(targetFolder, ActionType.WRITE, user)).thenReturn(true);
		when(permissionService.check(targetFolder, ActionType.MANAGE_PERMISSIONS, user)).thenReturn(true);
		when(permissionService.check(parentFolder, ActionType.READ, user)).thenReturn(true);
		when(permissionService.check(parentFolder, ActionType.WRITE, user)).thenReturn(false);
		when(permissionService.check(parentFolder, ActionType.MANAGE_PERMISSIONS, user)).thenReturn(true);

		assertTrue(checker.checkEntityAccessById(14L, ActionType.READ, user));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.WRITE, user));
		assertTrue(checker.checkEntityAccessById(14L, ActionType.MANAGE_PERMISSIONS, user));
		assertTrue(checker.checkEntityAccessById(15L, ActionType.READ, user));
		assertFalse(checker.checkEntityAccessById(15L, ActionType.WRITE, user));
		assertFalse(checker.checkEntityAccessById(15L, ActionType.MANAGE_PERMISSIONS, user));

		assertTrue(checker.checkLowerAccessById(14L, ActionType.READ, user));
		assertTrue(checker.checkLowerAccessById(14L, ActionType.WRITE, user));
		assertTrue(checker.checkLowerAccessById(14L, ActionType.MANAGE_PERMISSIONS, user));
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
}
