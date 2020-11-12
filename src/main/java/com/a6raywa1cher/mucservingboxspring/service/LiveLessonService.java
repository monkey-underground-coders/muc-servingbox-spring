package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface LiveLessonService {
	LiveLesson schedule(String name, LessonSchema schema, ZonedDateTime start, ZonedDateTime end, User creator);

	LiveLesson start(String name, LessonSchema schema, ZonedDateTime end, User creator);

	LiveLesson startOnTheFly(String name, ZonedDateTime end, User creator);

	Page<LiveLesson> getPageByCreator(BooleanExpression filter, Pageable pageable, User creator);

	Page<LiveLesson> getPage(BooleanExpression filter, Pageable pageable);

	List<LiveLesson> getActiveList();

	Page<LiveLesson> getPageBySchema(LessonSchema schema, Pageable pageable);

	Optional<LiveLesson> getById(Long id);

	LiveLesson edit(LiveLesson lesson, String name, ZonedDateTime start, ZonedDateTime end);

	LiveLesson connect(LiveLesson lesson, User user);

	LiveLesson stop(LiveLesson lesson);

	void delete(LiveLesson lesson);
}
