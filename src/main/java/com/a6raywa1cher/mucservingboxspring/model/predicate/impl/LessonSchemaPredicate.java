package com.a6raywa1cher.mucservingboxspring.model.predicate.impl;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.lesson.QLiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.predicate.AbstractPredicate;
import com.a6raywa1cher.mucservingboxspring.model.predicate.SearchCriteria;
import com.querydsl.core.types.dsl.*;

public class LessonSchemaPredicate extends AbstractPredicate {

	public LessonSchemaPredicate(SearchCriteria criteria) {
		super(criteria, "lessonSchema");
	}

	public LessonSchemaPredicate(SearchCriteria criteria, String variableName) {
		super(criteria, variableName);
	}

	@Override
	protected PathBuilder<LessonSchema> getPathBuilder() {
		return new PathBuilder<>(LessonSchema.class, variableName);
	}

	@Override
	public BooleanExpression getPredicate() {
		if (criteria.getKey().equals("description")) {
			if (criteria.getOperation().equalsIgnoreCase(":")) {
				return Expressions.booleanTemplate("fts('russian', " + variableName + ".description" + ", {0}) = true", criteria.getValue());
			}
		}
		return super.getPredicate();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T, J extends EntityPathBase<T>> ListPath<T, J> bindList(String variableName) {
		if (variableName.equals("liveLessons")) {
			return (ListPath<T, J>)
				getPathBuilder().getList("liveLessons", LiveLesson.class, QLiveLesson.class);
		}
		return super.bindList(variableName);
	}
}
