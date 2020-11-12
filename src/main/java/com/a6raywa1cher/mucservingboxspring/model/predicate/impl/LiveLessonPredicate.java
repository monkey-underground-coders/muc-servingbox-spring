package com.a6raywa1cher.mucservingboxspring.model.predicate.impl;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.predicate.AbstractPredicate;
import com.a6raywa1cher.mucservingboxspring.model.predicate.SearchCriteria;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.TimePath;

import java.time.ZonedDateTime;

public class LiveLessonPredicate extends AbstractPredicate {
	public LiveLessonPredicate(SearchCriteria criteria) {
		super(criteria, "liveLesson");
	}

	@Override
	protected PathBuilder<LiveLesson> getPathBuilder() {
		return new PathBuilder<>(LiveLesson.class, "liveLesson");
	}

	public BooleanExpression getPredicate() {
		PathBuilder<LiveLesson> entityPath = getPathBuilder();
		if (criteria.getKey().startsWith("schema.")) {
			return this.delegate(LessonSchemaPredicate.class, criteria);
		}
		if (criteria.getKey().startsWith("creator.")) {
			return this.delegate(UserPredicate.class, criteria);
		}
		if (criteria.getKey().equals("startAt") ||
			criteria.getKey().equals("endAt") ||
			criteria.getKey().equals("createdAt")) {
			TimePath<ZonedDateTime> path = entityPath.getTime(criteria.getKey(), ZonedDateTime.class);
			ZonedDateTime value = ZonedDateTime.parse((String) criteria.getValue());
			switch (criteria.getOperation()) {
				case ":":
					return path.eq(value);
				case ">":
					return path.goe(value);
				case "<":
					return path.loe(value);
			}
		}

		return super.getPredicate();
	}
}
