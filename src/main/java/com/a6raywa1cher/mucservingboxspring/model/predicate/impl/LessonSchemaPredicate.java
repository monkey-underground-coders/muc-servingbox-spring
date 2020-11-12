package com.a6raywa1cher.mucservingboxspring.model.predicate.impl;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.predicate.AbstractPredicate;
import com.a6raywa1cher.mucservingboxspring.model.predicate.SearchCriteria;
import com.querydsl.core.types.dsl.PathBuilder;

public class LessonSchemaPredicate extends AbstractPredicate {

	public LessonSchemaPredicate(SearchCriteria criteria) {
		super(criteria, "lessonSchema");
	}

	public LessonSchemaPredicate(SearchCriteria criteria, String variableName) {
		super(criteria, variableName);
	}

	@Override
	protected PathBuilder<?> getPathBuilder() {
		return new PathBuilder<>(LessonSchema.class, variableName);
	}
}
