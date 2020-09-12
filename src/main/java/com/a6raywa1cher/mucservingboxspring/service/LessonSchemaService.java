package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LessonSchemaService {
	LessonSchema create(String title, String description, User creator);

	Optional<LessonSchema> getById(Long id);

	Page<LessonSchema> getPage(List<String> searchWords, Pageable pageable);

	LessonSchema editSchema(LessonSchema lessonSchema, String title, String description);

	void deleteSchema(LessonSchema lessonSchema);
}
