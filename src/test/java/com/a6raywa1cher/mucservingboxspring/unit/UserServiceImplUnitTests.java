package com.a6raywa1cher.mucservingboxspring.unit;


import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityRepository;
import com.a6raywa1cher.mucservingboxspring.model.repo.UserRepository;
import com.a6raywa1cher.mucservingboxspring.service.impl.UserServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplUnitTests {
	@Mock
	private UserRepository repository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private FSEntityRepository fsEntityRepository;
	private final String temporaryUserName = "Sanya";
	private final Duration temporaryUserAccessDuration = Duration.ofDays(1L);

	@Test
	public void deleteUserTest() {
		UserServiceImpl userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName);

		User user = new User();

		userService.deleteUser(user);
		verify(repository).delete(user);
	}

	@Test
	public void createTempUserTest() {
		UserServiceImpl userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName);

		List<User> saved = getSaveTracker();

		User output = userService.create("830");

		User user = User.builder()
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
		assertEquals(user, toCheck);

	}

	@Test
	public void createTest() {
		UserServiceImpl userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName);

		List<User> saved = getSaveTracker();
		when(passwordEncoder.encode("123")).thenReturn("123");

		User output = userService.create(UserRole.STUDENT, "Sanya", "Sanya", "123", "890");

		User user = User.builder()
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
		assertEquals(user, toCheck);
	}

	@Test
	public void editPasswordTest(){
		UserServiceImpl userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName);

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
		UserServiceImpl userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName);

		FSEntity targetFolder = FSEntity.createFolder("/f2/", null, false);
		FSEntity expectedFolder =  FSEntity.createFolder("/f1/", null, false);


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
		UserServiceImpl userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName);


		ZonedDateTime targetZonedDateTime = ZonedDateTime.of(2020, 12, 3, 12, 32, 59, 232, ZoneId.of("UTC"));
		ZonedDateTime expectedZoneDateTime = ZonedDateTime.of(2020, 12, 3, 15, 32, 59, 232, ZoneId.of("UTC"));

		User user = User.builder()
			.lastVisitAt(targetZonedDateTime)
			.build();

		List<User> saved = getSaveTracker();
		User output = userService.setLastVisitAt(user, expectedZoneDateTime);

		User toCheck = saved.get(0);

		assertEquals(toCheck, output);
		assertEquals(expectedZoneDateTime, toCheck.getLastVisitAt());
	}

	@Test
	public void editUserTest() {
		UserServiceImpl userService = new UserServiceImpl(repository, passwordEncoder, temporaryUserAccessDuration,
			temporaryUserName);

		User user = User.builder()
			.username("Dolban")
			.name("Rolan")
			.build();

		List<User> saved = getSaveTracker();
		User output = userService.editUser(user, null, "Gay", "Gleb");

		User toCheck = saved.get(0);

		assertEquals(toCheck, output);
		assertEquals("Gay", toCheck.getUsername());
		assertEquals("Gleb", toCheck.getName());

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
