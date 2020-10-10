package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.component.ExpiredPermissionEntitiesRemoverComponent;
import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.repo.LiveLessonRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.LiveLessonService;
import com.a6raywa1cher.mucservingboxspring.utils.hibernate.FullTextSearchSQLFunction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LiveLessonServiceImpl implements LiveLessonService {
	private final LiveLessonRepository repository;
	private final LessonSchemaService schemaService;
	private final FSEntityService fsEntityService;
	private final FSEntityPermissionService permissionService;
	private final ExpiredPermissionEntitiesRemoverComponent removerComponent;

	public LiveLessonServiceImpl(LiveLessonRepository repository, LessonSchemaService schemaService,
								 FSEntityService fsEntityService, FSEntityPermissionService permissionService,
								 ExpiredPermissionEntitiesRemoverComponent removerComponent) {
		this.repository = repository;
		this.schemaService = schemaService;
		this.fsEntityService = fsEntityService;
		this.permissionService = permissionService;
		this.removerComponent = removerComponent;
	}

	private static ZonedDateTime getStartFromNow() {
		return ZonedDateTime.now().plus(100, ChronoUnit.MILLIS);
	}

	@Override
	public LiveLesson schedule(String name, LessonSchema schema, ZonedDateTime start, ZonedDateTime end, User creator) {
		LiveLesson liveLesson = new LiveLesson();
		liveLesson.setName(name);
		liveLesson.setSchema(schema);
		liveLesson.setStartAt(start);
		liveLesson.setEndAt(end);
		liveLesson.setManagedStudentPermissions(new ArrayList<>());
		liveLesson.setCreatedAt(ZonedDateTime.now());
		liveLesson.setCreator(creator);
		LiveLesson saved = repository.save(liveLesson);
		saved.setRoot(fsEntityService.createNewLiveLessonRoot(liveLesson));
		return repository.save(saved);
	}

	@Override
	public LiveLesson start(String name, LessonSchema schema, ZonedDateTime end, User creator) {
		return schedule(name, schema, getStartFromNow(), end, creator);
	}

	@Override
	public LiveLesson startOnTheFly(String name, ZonedDateTime end, User creator) {
		LessonSchema lessonSchema = schemaService.createOnFly(creator);
		return schedule(name, lessonSchema, getStartFromNow(), end, creator);
	}

	@Override
	public Page<LiveLesson> getPageByCreator(List<String> searchWords, Pageable pageable, User creator) {
		return repository.findByNameAndUser(FullTextSearchSQLFunction.searchWordsToQueryParam(searchWords),
			creator, pageable);
	}

	@Override
	public Page<LiveLesson> getPage(List<String> searchWords, Pageable pageable) {
		return repository.findByName(FullTextSearchSQLFunction.searchWordsToQueryParam(searchWords),
			pageable);
	}

	@Override
	public Page<LiveLesson> getPageBySchema(LessonSchema schema, Pageable pageable) {
		return repository.findBySchema(schema, pageable);
	}

	@Override
	public LiveLesson edit(LiveLesson lesson, String name, ZonedDateTime start, ZonedDateTime end) {
		lesson.setName(name);
		lesson.setStartAt(start);
		lesson.setEndAt(end);
		return repository.save(lesson);
	}

	@Override
	public FSEntity connect(LiveLesson lesson, User user) {
		FSEntity folder = fsEntityService.createNewFolder(lesson.getRoot(), Long.toString(user.getId()),
			false, user);
		FSEntityPermission permission = permissionService.create(List.of(folder), List.of(user), new ArrayList<>(),
			true, List.of(ActionType.READ, ActionType.WRITE));
		lesson.getManagedStudentPermissions().add(permission);
		repository.save(lesson);
		return folder;
	}

	@Override
	public LiveLesson stop(LiveLesson lesson) {
		lesson.setEndAt(ZonedDateTime.now().minus(500, ChronoUnit.MILLIS));
		LiveLesson save = repository.save(lesson);
		removerComponent.removeExpiredPermissions();
		return save;
	}

	@Override
	public void delete(LiveLesson lesson) {
		fsEntityService.deleteEntity(lesson.getRoot());
		permissionService.delete(lesson.getManagedStudentPermissions());
		lesson.setManagedStudentPermissions(new ArrayList<>());
		LiveLesson saved = repository.save(lesson);
		repository.delete(saved);
	}
}