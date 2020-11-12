package com.a6raywa1cher.mucservingboxspring.utils;

import com.a6raywa1cher.mucservingboxspring.model.predicate.AbstractPredicate;
import com.a6raywa1cher.mucservingboxspring.model.predicate.PredicatesBuilder;
import com.a6raywa1cher.mucservingboxspring.rest.exc.FilterParsingLexicalException;
import com.a6raywa1cher.mucservingboxspring.rest.exc.FilterParsingSyntaxException;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestUtils {
	public static BooleanExpression decodeFilter(String filter, Class<? extends AbstractPredicate> predicateClass) {
		if (filter == null) return Expressions.asBoolean(true).isTrue();
		PredicatesBuilder builder = new PredicatesBuilder(predicateClass);
		Pattern pattern = Pattern.compile("([a-zA-Z0-9.]+?)([:<>])'(.+?)',");
		Matcher matcher = pattern.matcher(filter + ",");
		while (matcher.find()) {
			String key = matcher.group(1);
			String operation = matcher.group(2);
			String value = LocalHtmlUtils.htmlEscape(matcher.group(3));
			builder.with(key, operation, value);
		}
		BooleanExpression build;
		try {
			build = builder.build();
		} catch (Exception e) {
			throw new FilterParsingSyntaxException(e);
		}
		if (build == null) {
			throw new FilterParsingLexicalException();
		}
		return build;
	}
}
