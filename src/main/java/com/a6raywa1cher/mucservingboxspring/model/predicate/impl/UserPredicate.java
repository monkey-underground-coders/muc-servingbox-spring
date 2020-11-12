package com.a6raywa1cher.mucservingboxspring.model.predicate.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.predicate.AbstractPredicate;
import com.a6raywa1cher.mucservingboxspring.model.predicate.SearchCriteria;
import com.querydsl.core.types.dsl.PathBuilder;

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
}
