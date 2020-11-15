package com.a6raywa1cher.mucservingboxspring.model.predicate;

import com.querydsl.core.types.dsl.*;
import lombok.SneakyThrows;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public abstract class AbstractPredicate {
	protected final SearchCriteria criteria;
	protected final String variableName;

	public AbstractPredicate(SearchCriteria criteria, String variableName) {
		this.criteria = criteria;
		this.variableName = variableName;
	}

	protected abstract PathBuilder<?> getPathBuilder();

	protected <T, J extends EntityPathBase<T>> ListPath<T, J> bindList(String variableName) {
		return null;
	}

	;

	public BooleanExpression getPredicate() {
		PathBuilder<?> entityPath = getPathBuilder();
		if (isNumeric(criteria.getValue().toString())) {
			NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
			int value = Integer.parseInt(criteria.getValue().toString());
			switch (criteria.getOperation()) {
				case ":":
					return path.eq(value);
				case ">":
					return path.goe(value);
				case "<":
					return path.loe(value);
			}
		} else if (criteria.getKey().startsWith("[") && criteria.getKey().endsWith("]")) {
			String name = criteria.getKey().substring(0, criteria.getKey().length() - 1);
			if (getPathBuilder().get(name) == null) return null;

			ListPath<?, ? extends EntityPathBase<?>> path = bindList(name);
			if (path == null) return null;

			if (!isNumeric(criteria.getValue().toString())) return null;
			int value = Integer.parseInt(criteria.getValue().toString());
			switch (criteria.getOperation()) {
				case ":":
					return path.size().eq(value);
				case ">":
					return path.size().goe(value);
				case "<":
					return path.size().loe(value);
			}
		} else {
			StringPath path = entityPath.getString(criteria.getKey());
			if (criteria.getOperation().equalsIgnoreCase(":")) {
				return path.containsIgnoreCase(criteria.getValue().toString());
			}
		}
		return null;
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
}
