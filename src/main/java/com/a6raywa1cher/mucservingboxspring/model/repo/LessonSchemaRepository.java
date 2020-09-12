package com.a6raywa1cher.mucservingboxspring.model.repo;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonSchemaRepository extends PagingAndSortingRepository<LessonSchema, Long> {
	@Query("from LessonSchema ls where fts('russian', ls.title, :searchQuery) = true")
	Page<LessonSchema> findByTitle(@Param("searchQuery") String searchQuery, Pageable pageable);
}
