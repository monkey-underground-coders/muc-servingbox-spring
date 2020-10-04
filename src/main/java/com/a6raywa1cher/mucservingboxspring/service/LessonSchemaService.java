package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LessonSchemaService {
	LessonSchema create(String title, String description, User creator);

	LessonSchema createOnFly(User creator);

	Optional<LessonSchema> getById(Long id);

	Page<LessonSchema> getPage(List<String> searchWords, Pageable pageable);

	Page<LessonSchema> getPage(List<String> searchWords, User creator, Pageable pageable);

	LessonSchema editSchema(LessonSchema lessonSchema, String title, String description);

	LessonSchema cloneSchema(LessonSchema lessonSchema, User user);

	void deleteSchema(LessonSchema lessonSchema);
}
