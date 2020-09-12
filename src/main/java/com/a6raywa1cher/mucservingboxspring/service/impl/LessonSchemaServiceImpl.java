package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.repo.LessonSchemaRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LessonSchemaServiceImpl implements LessonSchemaService {
	private final FSEntityService service;
	private final LessonSchemaRepository repository;

	@Autowired
	public LessonSchemaServiceImpl(FSEntityService service, LessonSchemaRepository repository) {
		this.service = service;
		this.repository = repository;
	}

	@Override
	public LessonSchema create(String title, String description, User creator) {
		LessonSchema lessonSchema = new LessonSchema();
		lessonSchema.setTitle(title);
		lessonSchema.setDescription(description);
		lessonSchema.setCreator(creator);
		LessonSchema saved = repository.save(lessonSchema);
		FSEntity fsEntity = service.createNewLessonRoot(saved);
		lessonSchema.setGenericFiles(fsEntity);
		return repository.save(saved);
	}

	@Override
	public Optional<LessonSchema> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Page<LessonSchema> getPage(List<String> searchWords, Pageable pageable) {
		return repository.findByTitle(String.join(" & ", searchWords), pageable);
	}

	@Override
	public LessonSchema editSchema(LessonSchema lessonSchema, String title, String description) {
		lessonSchema.setTitle(title);
		lessonSchema.setDescription(description);
		return repository.save(lessonSchema);
	}

	@Override
	public void deleteSchema(LessonSchema lessonSchema) {
		repository.delete(lessonSchema);
	}
}
