package com.a6raywa1cher.mucservingboxspring.utils;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.type.IntegerType;

public class PostgreSqlDialectFixed extends PostgreSQL10Dialect {
	public PostgreSqlDialectFixed() {
		super();
		registerFunction("bitwise_and", new BitwiseAndFunction("bitwise_and", IntegerType.INSTANCE));
	}
}
