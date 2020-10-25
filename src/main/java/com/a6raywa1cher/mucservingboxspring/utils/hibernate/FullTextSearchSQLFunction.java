package com.a6raywa1cher.mucservingboxspring.utils.hibernate;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;
import org.hibernate.type.Type;

import java.util.List;

public class FullTextSearchSQLFunction implements SQLFunction {
	public static String searchWordsToQueryParam(List<String> searchWords) {
		return String.join(" & ", searchWords);
	}

	@Override
	public Type getReturnType(Type columnType, Mapping mapping)
		throws QueryException {
		return new BooleanType();
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public boolean hasParenthesesIfNoArguments() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String render(Type type, List args, SessionFactoryImplementor factory)
		throws QueryException {
		if (args == null || args.size() < 2) {
			throw new IllegalArgumentException(
				"The function must be passed 2 arguments");
		}

		String fragment;
		String ftsConfig;
		String field;
		String value;
		if (args.size() == 3) {
			ftsConfig = (String) args.get(0);
			field = (String) args.get(1);
			value = (String) args.get(2);
			fragment = "to_tsvector(" + ftsConfig + "::regconfig, " + field + ")" + " @@ to_tsquery(" + ftsConfig + ", " + value + ")";
		} else {
			field = (String) args.get(0);
			value = (String) args.get(1);
			fragment = "to_tsvector(" + field + ")" + " @@ to_tsquery(" + value + ")";
		}
		return fragment;
	}
}