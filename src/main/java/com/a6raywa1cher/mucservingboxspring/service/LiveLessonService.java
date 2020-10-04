package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;

public interface LiveLessonService {
	LiveLesson schedule(String name, LessonSchema schema, ZonedDateTime start, ZonedDateTime end, User creator);

	LiveLesson start(String name, LessonSchema schema, ZonedDateTime end, User creator);

	LiveLesson startOnTheFly(String name, ZonedDateTime end, User creator);

	Page<LiveLesson> getPageByCreator(List<String> searchWords, Pageable pageable, User creator);

	Page<LiveLesson> getPage(List<String> searchWords, Pageable pageable);

	Page<LiveLesson> getPageBySchema(LessonSchema schema, Pageable pageable);

	LiveLesson edit(LiveLesson lesson, String name, ZonedDateTime start, ZonedDateTime end);

	FSEntity connect(LiveLesson lesson, User user);

	LiveLesson stop(LiveLesson lesson);

	void delete(LiveLesson lesson);
}
