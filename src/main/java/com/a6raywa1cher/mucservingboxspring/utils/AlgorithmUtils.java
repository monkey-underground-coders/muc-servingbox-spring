package com.a6raywa1cher.mucservingboxspring.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AlgorithmUtils {
	public static List<Integer> getSlashes(String path) {
		return IntStream.range(0, path.length())
			.filter(i -> path.charAt(i) == '/')
			.boxed()
			.collect(Collectors.toList());
	}

	public static long count(String str, char c) {
		return str.chars()
			.filter(c1 -> c1 == c)
			.count();
	}
}
