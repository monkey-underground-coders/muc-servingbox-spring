package com.a6raywa1cher.mucservingboxspring.unit;


import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.repo.UserRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.service.impl.UserServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplUnitTests {
	@Mock
	private UserRepository repository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private LessonSchemaService lessonSchemaService;
	@Mock
	private FSEntityService fsEntityService;
	private final String temporaryUserName = "Sanya";
	private final Duration temporaryUserAccessDuration = Duration.ofDays(1L);

	@Test
	public void deleteUserTest() {
		UserService userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName, lessonSchemaService, fsEntityService);

		LessonSchema lessonSchema = new LessonSchema();
		FSEntity rootFolder = FSEntity.createFolder("/f1/", null, false);
		User user = User.builder()
			.rootFolder(rootFolder)
			.schemaList(List.of(lessonSchema))
			.build();

		userService.deleteUser(user);

		verify(repository).delete(user);
		verify(lessonSchemaService).deleteSchema(lessonSchema);
		verify(fsEntityService).deleteEntity(rootFolder);
	}

	@Test
	public void createTempUserTest() {
		UserService userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName, lessonSchemaService, fsEntityService);

		List<User> saved = getSaveTracker();

		User output = userService.create("830");

		User reference = User.builder()
			.username(output.getUsername())
			.password(output.getPassword())
			.name(output.getName())
			.userRole(UserRole.TEMPORARY_USER)
			.schemaList(new ArrayList<>())
			.expiringAt(output.getExpiringAt())
			.createdAt(output.getCreatedAt())
			.createdIp("830")
			.lastVisitAt(output.getLastVisitAt())
			.build();

		User toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals(reference, toCheck);
		assertNotNull(output.getExpiringAt());
		assertTrue(output.getExpiringAt().isAfter(ZonedDateTime.now()));
	}

	@Test
	public void createTest() {
		UserService userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName, lessonSchemaService, fsEntityService);

		List<User> saved = getSaveTracker();
		when(passwordEncoder.encode("123")).thenReturn("123");

		User output = userService.create(UserRole.STUDENT, "Sanya", "Sanya", "123", "890");

		User reference = User.builder()
			.username("Sanya")
			.password("123")
			.name("Sanya")
			.userRole(UserRole.STUDENT)
			.schemaList(new ArrayList<>())
			.expiringAt(null)
			.createdAt(output.getCreatedAt())
			.createdIp("890")
			.lastVisitAt(output.getLastVisitAt())
			.build();

		User toCheck = saved.get(0);

		assertEquals(toCheck, output);
		assertEquals(reference, toCheck);
	}

	@Test
	public void editPasswordTest() {
		UserService userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName, lessonSchemaService, fsEntityService);

		User user = User.builder()
			.password("321")
			.build();

		List<User> saved = getSaveTracker();
		when(passwordEncoder.encode("123")).thenReturn("123");

		User output = userService.editPassword(user, "123");

		User toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals("123", toCheck.getPassword());
	}

	@Test
	public void editRootFolderTest() {
		UserService userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName, lessonSchemaService, fsEntityService);

		FSEntity targetFolder = FSEntity.createFolder("/f2/", null, false);
		FSEntity expectedFolder = FSEntity.createFolder("/f1/", null, false);
		User user = User.builder()
			.rootFolder(targetFolder)
			.build();

		List<User> saved = getSaveTracker();

		User output = userService.editRootFolder(user, expectedFolder);

		User toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals(expectedFolder, toCheck.getRootFolder());
	}

	@Test
	public void setLastVisitAt() {
		UserService userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName, lessonSchemaService, fsEntityService);


		ZonedDateTime previousVisitTime = ZonedDateTime.now();
		ZonedDateTime newVisitTime = ZonedDateTime.now().plusDays(3);
		User user = User.builder()
			.lastVisitAt(previousVisitTime)
			.build();

		List<User> saved = getSaveTracker();

		User output = userService.setLastVisitAt(user, newVisitTime);

		User toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals(newVisitTime, toCheck.getLastVisitAt());
	}

	@Test
	public void editUserTest() {
		UserService userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName, lessonSchemaService, fsEntityService);

		User user = User.builder()
			.username("Username")
			.name("Rolan")
			.build();

		List<User> saved = getSaveTracker();

		User output = userService.editUser(user, null, "TheSamePerson", "Roland");

		User toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals("TheSamePerson", toCheck.getUsername());
		assertEquals("Roland", toCheck.getName());
	}

	private List<User> getSaveTracker() {
		List<User> saved = new ArrayList<>();
		when(repository.save(any()))
			.then((inv) -> {
				User e = inv.getArgument(0);
				saved.add(e);
				return e;
			});
		return saved;
	}
}
