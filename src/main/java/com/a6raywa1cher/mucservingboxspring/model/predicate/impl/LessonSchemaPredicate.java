package com.a6raywa1cher.mucservingboxspring.model.predicate.impl;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.lesson.QLiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.predicate.AbstractPredicate;
import com.a6raywa1cher.mucservingboxspring.model.predicate.SearchCriteria;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

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
	protected Expression<?> bind(String variableName) {
		if (variableName.equals("liveLessons")) {
			return getPathBuilder().getList("liveLessons", LiveLesson.class, QLiveLesson.class);
		}
		return super.bind(variableName);
	}
}
