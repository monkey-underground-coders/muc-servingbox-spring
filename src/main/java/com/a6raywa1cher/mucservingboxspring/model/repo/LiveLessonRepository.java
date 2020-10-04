package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface LiveLessonRepository extends PagingAndSortingRepository<LiveLesson, Long> {
	@Query("from LiveLesson l where fts('russian', l.name, :searchQuery) = true and l.creator = :user")
	Page<LiveLesson> findByNameAndUser(@Param("searchQuery") String searchQuery, @Param("user") User creator, Pageable pageable);

	@Query("from LiveLesson l where fts('russian', l.name, :searchQuery) = true")
	Page<LiveLesson> findByName(@Param("searchQuery") String searchQuery, Pageable pageable);

	Page<LiveLesson> findBySchema(LessonSchema schema, Pageable pageable);

	@Query("from LiveLesson l where l.endAt is not null and l.endAt < :now and size(l.managedStudentPermissions) > 0")
	List<LiveLesson> findExpired(@Param("now") ZonedDateTime now);
}
