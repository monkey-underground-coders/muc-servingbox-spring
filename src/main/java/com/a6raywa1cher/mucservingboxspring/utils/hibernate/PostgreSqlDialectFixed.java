package com.a6raywa1cher.mucservingboxspring.utils.hibernate;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.type.IntegerType;

public class PostgreSqlDialectFixed extends PostgreSQL10Dialect {
	public PostgreSqlDialectFixed() {
		super();
		registerFunction("bitwise_and", new BitwiseAndStandardSQLFunction("bitwise_and", IntegerType.INSTANCE));
		registerFunction("fts", new FullTextSearchSQLFunction());
	}
}
