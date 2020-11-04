package com.a6raywa1cher.mucservingboxspring.unit;


import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.security.MvcAccessChecker;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.utils.AuthenticationResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

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

	@Test
	public void checkFileAccessPath() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver);

		FSEntity targetFile = FSEntity.createFile("/f1/meow", "/134", 25L,
			mock(User.class), false);
		User user1 = mock(User.class, "user1");
		Authentication authentication1 = mock(Authentication.class);
		User user2 = mock(User.class, "user2");
		Authentication authentication2 = mock(Authentication.class);

		when(fsEntityService.getById(14L)).thenReturn(Optional.of(targetFile));
		when(permissionService.check(targetFile, ActionType.READ, user1)).thenReturn(true);
		when(permissionService.check(targetFile, ActionType.WRITE, user1)).thenReturn(false);
		when(permissionService.check(targetFile, ActionType.MANAGE_PERMISSIONS, user1)).thenReturn(false);
		when(permissionService.check(targetFile, ActionType.READ, user2)).thenReturn(false);
		when(permissionService.check(targetFile, ActionType.WRITE, user2)).thenReturn(true);
		when(permissionService.check(targetFile, ActionType.MANAGE_PERMISSIONS, user2)).thenReturn(false);
		when(resolver.getUser(authentication1)).thenReturn(user1);
		when(resolver.getUser(authentication2)).thenReturn(user2);

		assertTrue(checker.checkEntityAccessById(14L, ActionType.READ, authentication1));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.WRITE, authentication1));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.MANAGE_PERMISSIONS, authentication1));

		assertFalse(checker.checkEntityAccessById(14L, ActionType.READ, authentication2));
		assertTrue(checker.checkEntityAccessById(14L, ActionType.WRITE, authentication2));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.MANAGE_PERMISSIONS, authentication2));
	}

	@Test
	public void checkFolderAccessPath() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver);

		FSEntity targetFolder = FSEntity.createFolder("/f1/f2/", mock(User.class), false);
		FSEntity parentFolder = FSEntity.createFolder("/f1/", mock(User.class), false);
		User user = mock(User.class, "user");
		Authentication authentication = mock(Authentication.class);

		when(fsEntityService.getById(14L)).thenReturn(Optional.of(targetFolder));
		when(fsEntityService.getById(15L)).thenReturn(Optional.of(parentFolder));
		when(fsEntityService.getParent(targetFolder)).thenReturn(Optional.of(parentFolder));
		when(permissionService.check(targetFolder, ActionType.READ, user)).thenReturn(true);
		when(permissionService.check(targetFolder, ActionType.WRITE, user)).thenReturn(true);
		when(permissionService.check(targetFolder, ActionType.MANAGE_PERMISSIONS, user)).thenReturn(true);
		when(permissionService.check(parentFolder, ActionType.READ, user)).thenReturn(true);
		when(permissionService.check(parentFolder, ActionType.WRITE, user)).thenReturn(false);
		when(permissionService.check(parentFolder, ActionType.MANAGE_PERMISSIONS, user)).thenReturn(true);
		when(resolver.getUser(authentication)).thenReturn(user);

		assertTrue(checker.checkEntityAccessById(14L, ActionType.READ, authentication));
		assertFalse(checker.checkEntityAccessById(14L, ActionType.WRITE, authentication));
		assertTrue(checker.checkEntityAccessById(14L, ActionType.MANAGE_PERMISSIONS, authentication));
		assertTrue(checker.checkEntityAccessById(15L, ActionType.READ, authentication));
		assertFalse(checker.checkEntityAccessById(15L, ActionType.WRITE, authentication));
		assertFalse(checker.checkEntityAccessById(15L, ActionType.MANAGE_PERMISSIONS, authentication));

		assertTrue(checker.checkLowerAccessById(14L, ActionType.READ, authentication));
		assertTrue(checker.checkLowerAccessById(14L, ActionType.WRITE, authentication));
		assertTrue(checker.checkLowerAccessById(14L, ActionType.MANAGE_PERMISSIONS, authentication));
	}

	@Test
	public void checkPermissionAccess() {
		MvcAccessChecker checker = new MvcAccessChecker(fsEntityService, permissionService, resolver);

		FSEntity targetFolder = FSEntity.createFolder("/f1/f2/", mock(User.class), false);
		FSEntity parentFolder = FSEntity.createFolder("/f1/", mock(User.class), false);
		FSEntityPermission fsEntityPermission = FSEntityPermission.builder()
			.entity(targetFolder)
			.build();
		User user = mock(User.class, "user");
		Authentication authentication = mock(Authentication.class);

		when(fsEntityService.getParent(targetFolder)).thenReturn(Optional.of(parentFolder));
		when(permissionService.check(parentFolder, ActionType.MANAGE_PERMISSIONS, user)).thenReturn(true);
		when(permissionService.getById(16L)).thenReturn(Optional.of(fsEntityPermission));
		when(resolver.getUser(authentication)).thenReturn(user);

		assertTrue(checker.checkPermissionAccess(16L, authentication));
	}
}
