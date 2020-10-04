package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.repo.LessonSchemaRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.utils.hibernate.FullTextSearchSQLFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

@Service
public class LessonSchemaServiceImpl implements LessonSchemaService {
	private final FSEntityService service;
	private final LessonSchemaRepository repository;

	@Value("${strings.lesson-schema.on-the-fly.title}")
	private String onTheFlyTitle;

	@Autowired
	public LessonSchemaServiceImpl(FSEntityService service, LessonSchemaRepository repository) {
		this.service = service;
		this.repository = repository;
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
		return repository.save(saved);
	}

	@Override
	public LessonSchema create(String title, String description, User creator) {
		return create(title, description, creator, false);
	}

	@Override
	public LessonSchema createOnFly(User creator) {
		String title = String.format(onTheFlyTitle, ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
		return create(title, null, creator, true);
	}

	@Override
	public Optional<LessonSchema> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Page<LessonSchema> getPage(List<String> searchWords, Pageable pageable) {
		return repository.findByTitle(FullTextSearchSQLFunction.searchWordsToQueryParam(searchWords), pageable);
	}

	@Override
	public Page<LessonSchema> getPage(List<String> searchWords, User creator, Pageable pageable) {
		return repository.findByTitleAndUser(FullTextSearchSQLFunction.searchWordsToQueryParam(searchWords), creator, pageable);
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
	public void deleteSchema(LessonSchema lessonSchema) {
		repository.delete(lessonSchema);
	}

	public String getOnTheFlyTitle() {
		return onTheFlyTitle;
	}

	public void setOnTheFlyTitle(String onTheFlyTitle) {
		this.onTheFlyTitle = onTheFlyTitle;
	}
}
