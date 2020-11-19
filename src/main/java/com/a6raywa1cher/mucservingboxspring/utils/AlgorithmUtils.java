package com.a6raywa1cher.mucservingboxspring.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AlgorithmUtils {
	public static List<Integer> getSlashes(String path) {
		return IntStream.range(0, path.length())
			.filter(i -> path.charAt(i) == '/')
			.boxed()
			.collect(Collectors.toList());
	}
}
