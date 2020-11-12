package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.lesson.QLiveLesson;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface LiveLessonRepository extends PagingAndSortingRepository<LiveLesson, Long>,
	QuerydslPredicateExecutor<LiveLesson>,
	QuerydslBinderCustomizer<QLiveLesson> {
	Page<LiveLesson> findBySchema(LessonSchema schema, Pageable pageable);

	@Query("from LiveLesson l where l.startAt < :now and l.endAt > :now")
	List<LiveLesson> findActive(@Param("now") ZonedDateTime now);

	@Query("from LiveLesson l where l.endAt is not null and l.endAt < :now and size(l.managedStudentPermissions) > 0")
	List<LiveLesson> findExpired(@Param("now") ZonedDateTime now);

	@Override
	default void customize(QuerydslBindings bindings, QLiveLesson root) {
		bindings.bind(root.name).first(StringExpression::containsIgnoreCase);
		bindings.excluding(root.managedStudentPermissions.any());
	}
}
