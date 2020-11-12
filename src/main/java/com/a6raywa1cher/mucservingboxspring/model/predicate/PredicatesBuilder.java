package com.a6raywa1cher.mucservingboxspring.model.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PredicatesBuilder {
	private final List<SearchCriteria> params;
	private final Class<? extends AbstractPredicate> clazz;

	public PredicatesBuilder(Class<? extends AbstractPredicate> clazz) {
		params = new ArrayList<>();
		this.clazz = clazz;
	}

	public PredicatesBuilder with(String key, String operation, Object value) {
		params.add(new SearchCriteria(key, operation, value));
		return this;
	}

	public BooleanExpression build() {
		if (params.size() == 0) {
			return null;
		}

		List<BooleanExpression> predicates = params.stream()
			.map(param -> {
				try {
					return clazz.getDeclaredConstructor(SearchCriteria.class).newInstance(param).getPredicate();
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException("Unreachable condition reached");
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		BooleanExpression result = Expressions.asBoolean(true).isTrue();
		for (BooleanExpression predicate : predicates) {
			result = result.and(predicate);
		}
		return result;
	}
}
