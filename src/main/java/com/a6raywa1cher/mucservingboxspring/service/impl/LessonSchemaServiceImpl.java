package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.lesson.QLessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.repo.LessonSchemaRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.LiveLessonService;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LessonSchemaServiceImpl implements LessonSchemaService {
	private final FSEntityService service;
	private final LessonSchemaRepository repository;
	private final FSEntityPermissionService permissionService;
	private LiveLessonService liveLessonService;

	@Value("${strings.lesson-schema.on-the-fly.title}")
	private String onTheFlyTitle;

	@Autowired
	public LessonSchemaServiceImpl(FSEntityService service, LessonSchemaRepository repository,
								   FSEntityPermissionService permissionService) {
		this.service = service;
		this.repository = repository;
		this.permissionService = permissionService;
	}

	public LessonSchemaServiceImpl(LiveLessonService liveLessonService, FSEntityService service,
								   LessonSchemaRepository repository, FSEntityPermissionService permissionService) {
		this.liveLessonService = liveLessonService;
		this.service = service;
		this.repository = repository;
		this.permissionService = permissionService;
	}

	@Autowired
	public void setLiveLessonService(LiveLessonService liveLessonService) {
		this.liveLessonService = liveLessonService;
	}

	private LessonSchema create(String title, String description, User creator, boolean onTheFly) {
		LessonSchema lessonSchema = new LessonSchema();
		lessonSchema.setTitle(title);
		lessonSchema.setDescription(description);
		lessonSchema.setCreator(creator);
		lessonSchema.setOnTheFly(onTheFly);
		LessonSchema saved = repository.save(lessonSchema);
		FSEntity fsEntity = service.createNewLessonRoot(saved);
		lessonSchema.setGenericFiles(fsEntity);
		permissionService.create(fsEntity, List.of(creator), new ArrayList<>(), true,
			List.of(ActionType.READ, ActionType.WRITE, ActionType.MANAGE_PERMISSIONS));
		return repository.save(saved);
	}

	@Override
	public LessonSchema create(String title, String description, User creator) {
		return create(title, description, creator, false);
	}

	@Override
	public LessonSchema createOnFly(User creator) {
		String title = String.format(onTheFlyTitle,
			ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
		return create(title, null, creator, true);
	}

	@Override
	public Optional<LessonSchema> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Page<LessonSchema> getPage(BooleanExpression filter, Pageable pageable) {
		return repository.findAll(filter, pageable);
	}

	@Override
	public Page<LessonSchema> getPage(BooleanExpression filter, User creator, Pageable pageable) {
		return repository.findAll(filter.and(QLessonSchema.lessonSchema.creator.eq(creator)), pageable);
	}

	@Override
	public LessonSchema editSchema(LessonSchema lessonSchema, String title, String description) {
		lessonSchema.setTitle(title);
		lessonSchema.setDescription(description);
		return repository.save(lessonSchema);
	}

	@Override
	public LessonSchema cloneSchema(LessonSchema oldSchema, User user) {
		LessonSchema newSchema = create(oldSchema.getTitle(), oldSchema.getDescription(), user, oldSchema.isOnTheFly());
		service.copyFolderContent(oldSchema.getGenericFiles(), newSchema.getGenericFiles(), false, user);
		return newSchema;
	}

	@Override
	public LessonSchema transferToNotOnFly(LessonSchema lessonSchema) {
		lessonSchema.setOnTheFly(false);
		return repository.save(lessonSchema);
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void deleteSchema(LessonSchema lessonSchema) {
		service.deleteEntity(lessonSchema.getGenericFiles());
		List<LiveLesson> liveLessonList = lessonSchema.getLiveLessons();
		if (liveLessonList != null) {
			liveLessonList.forEach(liveLessonService::delete);
		}
		repository.delete(lessonSchema);
	}

	public void setOnTheFlyTitle(String onTheFlyTitle) {
		this.onTheFlyTitle = onTheFlyTitle;
	}
}
