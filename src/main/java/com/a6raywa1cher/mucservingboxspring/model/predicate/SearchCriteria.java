package com.a6raywa1cher.mucservingboxspring.model.predicate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchCriteria {
	private String key;
	private String operation;
	private Object value;
}
