package com.a6raywa1cher.mucservingboxspring.model.predicate;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.*;
import lombok.SneakyThrows;

import java.time.ZonedDateTime;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public abstract class AbstractPredicate {
	protected final SearchCriteria criteria;
	protected final String variableName;

	public AbstractPredicate(SearchCriteria criteria, String variableName) {
		this.criteria = criteria;
		this.variableName = variableName;
	}

	protected abstract PathBuilder<?> getPathBuilder();

	protected Expression<?> bind(String variableName) {
		return null;
	}

	public static <T extends Comparable<?>> BooleanExpression appendNumberOperation(ComparableExpression<T> path, String operation, T value) {
		switch (operation) {
			case "=":
				return path.eq(value);
			case "!=":
				return path.ne(value);
			case ">=":
				return path.goe(value);
			case "<=":
				return path.loe(value);
			case ">":
				return path.gt(value);
			case "<":
				return path.lt(value);
			default:
				return null;
		}
	}

	@SneakyThrows
	protected BooleanExpression delegate(Class<? extends AbstractPredicate> predicate, SearchCriteria criteria) {
		String key = criteria.getKey();
		String fieldInTarget = key.substring(key.lastIndexOf('.') + 1);
		String target = (variableName.indexOf('.') != -1 ?
			variableName.substring(0, variableName.indexOf('.')) :
			variableName) + '.' + key.substring(0, key.lastIndexOf('.'));
		return predicate.getDeclaredConstructor(SearchCriteria.class, String.class)
			.newInstance(new SearchCriteria(
				fieldInTarget,
				criteria.getOperation(),
				criteria.getValue()
			), target).getPredicate();
	}

	public static <T extends Number & Comparable<?>> BooleanExpression appendNumberOperation(NumberExpression<T> path, String operation, T value) {
		switch (operation) {
			case "=":
				return path.eq(value);
			case "!=":
				return path.ne(value);
			case ">=":
				return path.goe(value);
			case "<=":
				return path.loe(value);
			case ">":
				return path.gt(value);
			case "<":
				return path.lt(value);
			default:
				return null;
		}
	}

	public BooleanExpression getPredicate() {
		PathBuilder<?> entityPath = getPathBuilder();
		if (bind(criteria.getKey()) != null) {
			Expression<?> expression = bind(criteria.getKey());
			if (expression instanceof NumberExpression) {
				return appendNumberOperation((NumberExpression<Integer>) expression, criteria.getOperation(), Integer.parseInt((String) criteria.getValue()));
			} else if (expression instanceof TemporalExpression) {
				return appendNumberOperation((TemporalExpression<ZonedDateTime>) expression, criteria.getOperation(), ZonedDateTime.parse((String) criteria.getValue()));
			}
		} else if (criteria.getKey().startsWith("[") && criteria.getKey().endsWith("]")) {
			String name = criteria.getKey().substring(1, criteria.getKey().length() - 1);
			if (getPathBuilder().get(name) == null) return null;

			ListPath<?, ?> path = (ListPath<?, ?>) bind(name);
			if (path == null) return null;

			if (!isNumeric(criteria.getValue().toString())) return null;
			int value = Integer.parseInt(criteria.getValue().toString());
			return appendNumberOperation(path.size(), criteria.getOperation(), value);
		} else if (isNumeric(criteria.getValue().toString())) {
			NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
			int value = Integer.parseInt(criteria.getValue().toString());
			return appendNumberOperation(path, criteria.getOperation(), value);
		} else {
			StringPath path = entityPath.getString(criteria.getKey());
			if ("=".equals(criteria.getOperation())) {
				return path.containsIgnoreCase(criteria.getValue().toString());
			}
		}
		return null;
	}
}
