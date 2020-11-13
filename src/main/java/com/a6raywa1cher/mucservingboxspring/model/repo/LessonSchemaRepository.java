package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.QLessonSchema;
import com.querydsl.core.types.dsl.Expressions;
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

@Repository
public interface LessonSchemaRepository extends PagingAndSortingRepository<LessonSchema, Long>,
	QuerydslPredicateExecutor<LessonSchema>,
	QuerydslBinderCustomizer<QLessonSchema> {
	@Query("select ls from LessonSchema as ls where (fts('russian', ls.title, :searchQuery) = true) and ls.creator = :user")
	Page<LessonSchema> findByTitleAndUser(@Param("searchQuery") String searchQuery, @Param("user") User creator, Pageable pageable);

	@Query("from LessonSchema ls where fts('russian', ls.title, :searchQuery) = true")
	Page<LessonSchema> findByTitle(@Param("searchQuery") String searchQuery, Pageable pageable);

	Page<LessonSchema> findByCreator(User creator, Pageable pageable);

	@Override
	default void customize(QuerydslBindings bindings, QLessonSchema root) {
		bindings.bind(root.title).first(StringExpression::containsIgnoreCase);
		bindings.bind(root.description).first((sp, value) -> Expressions.booleanTemplate("fts('russian', {0}, {1}) = true", sp, value));
//		bindings.bind(root.description).first(StringExpression::containsIgnoreCase);
		bindings.excluding(root.genericFiles, root.liveLessons);
	}
}
