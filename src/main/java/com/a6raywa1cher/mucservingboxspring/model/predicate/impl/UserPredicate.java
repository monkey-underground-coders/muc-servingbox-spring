package com.a6raywa1cher.mucservingboxspring.model.predicate.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.predicate.AbstractPredicate;
import com.a6raywa1cher.mucservingboxspring.model.predicate.SearchCriteria;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;

import java.time.ZonedDateTime;

public class UserPredicate extends AbstractPredicate {
	public UserPredicate(SearchCriteria criteria) {
		super(criteria, "user");
	}

	public UserPredicate(SearchCriteria criteria, String variableName) {
		super(criteria, variableName);
	}

	@Override
	protected PathBuilder<?> getPathBuilder() {
		return new PathBuilder<>(User.class, variableName);
	}

	@Override
	protected Expression<?> bind(String variableName) {
		if (variableName.equals("expiringAt") ||
			variableName.equals("createdAt") ||
			variableName.equals("lastVisitAt")) {
			return getPathBuilder().getTime(variableName, ZonedDateTime.class);
		}
		return super.bind(variableName);
	}
}
