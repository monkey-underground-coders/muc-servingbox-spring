package com.a6raywa1cher.mucservingboxspring.integration;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Import(FSEntityPermissionServiceImplIntegrationTestsConfig.class)
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
@Transactional
@Slf4j
public class FSEntityPermissionServiceImplIntegrationTests {
	@Autowired
	FSEntityPermissionService permissionService;
	@Autowired
	FSEntityService fsEntityService;
	@Autowired
	UserService userService;

	User admin;
	User teacher;
	User student1;
	User student2;

	FSEntity teacherHome;
	FSEntity student1Home;
	FSEntity student2Home;

	@Before
	public void initialize() {
		admin = userService.getByUsername("admin").orElseThrow();
		teacher = userService.create(UserRole.TEACHER, "teacher", "teacher", "teacher", "");
		student1 = userService.create("");
		student2 = userService.create("");
		student1Home = fsEntityService.createNewHome(student1);
		student2Home = fsEntityService.createNewHome(student2);
		teacherHome = fsEntityService.createNewHome(teacher);
	}

	@Test
	public void contextLoads() {

	}

	@Test
	public void homeAccessCorrect() {
		long start = System.currentTimeMillis();
		FSEntity home = fsEntityService.getByPath(String.format("/user_home/%d/", student1.getId())).orElseThrow();

		checkPermissions(home, student1, true, true, true);
		checkPermissions(home, teacher, false, false, false);
		checkPermissions(home, admin, true, true, true);
		checkPermissions(home, student2, false, false, false);
		log.info("homeAccessCorrect {}ms", System.currentTimeMillis() - start);
	}

	@Test
	public void nestedPermissionsCheckCorrect() {
		long start = System.currentTimeMillis();
		log.info("start nestedPermissionsCheckCorrect");
		FSEntity publicFolder = fsEntityService.createNewFolder(
			teacherHome,
			"public",
			false,
			teacher
		);
		FSEntity publicFile = fsEntityService.createNewFile(
			publicFolder,
			"text.doc",
			mock(MultipartFile.class),
			false,
			teacher
		);
		assertNotNull(publicFile);
		FSEntityPermission permission = permissionService.create(
			publicFolder,
			new ArrayList<>(),
			List.of(UserRole.STUDENT, UserRole.TEMPORARY_USER),
			false,
			List.of(ActionType.READ)
		);

		checkPermissions(publicFolder, student1, true, false, false);
		checkPermissions(publicFile, student1, true, false, false);
		checkPermissions(teacherHome, student1, false, false, false);

		User teacher2 = userService.create(UserRole.TEACHER, "t2", "t2", "t2", "");
		checkPermissions(publicFile, teacher2, false, false, false);
		checkPermissions(publicFolder, teacher2, false, false, false);


		FSEntity publicFolder2 = fsEntityService.createNewFolder(
			publicFolder,
			"public2",
			false,
			teacher
		);
		FSEntity publicFile2 = fsEntityService.createNewFile(
			publicFolder2,
			"text2.doc",
			mock(MultipartFile.class),
			false,
			teacher
		);

		permissionService.create(
			publicFile,
			List.of(teacher2),
			new ArrayList<>(),
			permission.getApplicationDefined(),
			List.of(ActionType.READ, ActionType.WRITE)
		);

		permissionService.create(
			publicFolder2,
			List.of(teacher2),
			new ArrayList<>(),
			permission.getApplicationDefined(),
			List.of(ActionType.READ, ActionType.WRITE)
		);

		checkPermissions(publicFile, teacher2, true, true, false);
		checkPermissions(publicFolder, teacher2, false, false, false);

		checkPermissions(publicFile2, teacher2, true, true, false);
		log.info("homeAccessCorrect {}ms", System.currentTimeMillis() - start);
	}

	private void checkPermissions(FSEntity entity, User user, boolean read, boolean write, boolean managePermissions) {
		assertEquals(String.format("Read check on entity %s failed", entity.getPath()), read, permissionService.check(entity, ActionType.READ, user));
		assertEquals(String.format("Write check on entity %s failed", entity.getPath()), write, permissionService.check(entity, ActionType.WRITE, user));
		assertEquals(String.format("M_P check on entity %s failed", entity.getPath()), managePermissions, permissionService.check(entity, ActionType.MANAGE_PERMISSIONS, user));
	}
}
