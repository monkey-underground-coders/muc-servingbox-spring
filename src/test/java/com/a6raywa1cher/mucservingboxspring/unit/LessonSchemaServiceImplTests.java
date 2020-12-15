package com.a6raywa1cher.mucservingboxspring.unit;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.repo.LessonSchemaRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.LiveLessonService;
import com.a6raywa1cher.mucservingboxspring.service.impl.LessonSchemaServiceImpl;
import com.a6raywa1cher.mucservingboxspring.service.impl.LiveLessonServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LessonSchemaServiceImplTests {
	@Mock
	FSEntityService service;
	@Mock
	LessonSchemaRepository repository;
	@Mock
	FSEntityPermissionService permissionService;
	@Mock
	LiveLessonService liveLessonService;

	@Test
	public void createTest() {
		LessonSchemaService lessonSchemaService = new LessonSchemaServiceImpl(liveLessonService, service, repository, permissionService);

		List<LessonSchema> saved = getSaveTracker();
		User creatorUser = new User();

		LessonSchema output = lessonSchemaService.create("Lesson 1", "Test lesson", creatorUser);

		LessonSchema toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals("Lesson 1", toCheck.getTitle());
		assertEquals("Test lesson", toCheck.getDescription());
		assertEquals(creatorUser, toCheck.getCreator());
		assertFalse(toCheck.isOnTheFly());
	}

	@Test
	public void createOnFlyTest() {
		LessonSchemaServiceImpl lessonSchemaService = new LessonSchemaServiceImpl(liveLessonService, service, repository, permissionService);

		lessonSchemaService.setOnTheFlyTitle("Lesson one %s");
		List<LessonSchema> saved = getSaveTracker();
		User creatorUser = new User();

		LessonSchema output = lessonSchemaService.createOnFly(creatorUser);

		LessonSchema toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertTrue("Error: " + toCheck.getTitle(), toCheck.getTitle().startsWith("Lesson one"));
		assertNull(toCheck.getDescription());
		assertEquals(creatorUser, toCheck.getCreator());
		assertTrue(toCheck.isOnTheFly());
	}

	@Test
	public void editSchemaTest() {
		LessonSchemaService lessonSchemaService = new LessonSchemaServiceImpl(liveLessonService, service, repository, permissionService);

		LessonSchema lessonSchema = new LessonSchema();

		List<LessonSchema> saved = getSaveTracker();

		LessonSchema output = lessonSchemaService.editSchema(lessonSchema, "Lesson 1", "Test lesson");

		LessonSchema toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals("Test lesson", toCheck.getDescription());
		assertEquals("Lesson 1", toCheck.getTitle());
	}

	@Test
	public void transferToNotOnFly() {
		LessonSchemaService lessonSchemaService = new LessonSchemaServiceImpl(liveLessonService, service, repository, permissionService);

		LessonSchema lessonSchema = new LessonSchema();

		List<LessonSchema> saved = getSaveTracker();

		LessonSchema output = lessonSchemaService.transferToNotOnFly(lessonSchema);

		LessonSchema toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertFalse(toCheck.isOnTheFly());
	}

	@Test
	public void deleteSchemaTest() {
		LessonSchemaService lessonSchemaService = new LessonSchemaServiceImpl(liveLessonService, service, repository, permissionService);

		LiveLesson liveLesson = new LiveLesson();
		LessonSchema lessonSchema = LessonSchema.builder()
			.liveLessons(List.of(liveLesson))
			.build();

		lessonSchemaService.deleteSchema(lessonSchema);

		verify(liveLessonService).delete(liveLesson);
		verify(service).deleteEntity(lessonSchema.getGenericFiles());
		verify(repository).delete(lessonSchema);
	}

	@Test
	public void cloneSchemaTest() {
		LessonSchemaService lessonSchemaService = new LessonSchemaServiceImpl(liveLessonService, service, repository, permissionService);

		User creatorUser = new User();
		LessonSchema oldSchema = LessonSchema.builder()
			.title("Lesson 1")
			.description("Test lesson")
			.creator(creatorUser)
			.onTheFly(false)
			.build();

		List<LessonSchema> saved = getSaveTracker();
		lessonSchemaService.cloneSchema(oldSchema, creatorUser);
		LessonSchema newSchema = saved.get(0);

		verify(service).copyFolderContent(oldSchema.getGenericFiles(), newSchema.getGenericFiles(),
			false, creatorUser);

		assertNotSame(newSchema, oldSchema);
		assertEquals(oldSchema.getCreator(), newSchema.getCreator());
		assertEquals(oldSchema.getDescription(), newSchema.getDescription());
		assertEquals(oldSchema.getTitle(), newSchema.getTitle());
		assertEquals(oldSchema.isOnTheFly(), newSchema.isOnTheFly());
	}

	private List<LessonSchema> getSaveTracker() {
		List<LessonSchema> saved = new ArrayList<>();
		when(repository.save(any()))
			.then((inv) -> {
				LessonSchema e = inv.getArgument(0);
				saved.add(e);
				return e;
			});
		return saved;
	}
}
