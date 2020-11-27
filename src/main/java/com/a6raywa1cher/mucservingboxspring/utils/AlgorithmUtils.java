package com.a6raywa1cher.mucservingboxspring.utils;

import java.util.ArrayList;
import java.util.Collections;
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

	public static List<String> getUpperLevels(String path) {
		if (path == null) return new ArrayList<>();
		if (path.equals("/")) return Collections.singletonList(path);
		String finalPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		List<String> out = IntStream.range(0, finalPath.length())
			.filter(i -> finalPath.charAt(i) == '/')
			.mapToObj(i -> finalPath.substring(0, i + 1))
			.collect(Collectors.toList());
		out.add(path);
		return out;
	}

//	public static List<String> separatePath(String path) {
//		if (path == null || path.equals("/")) return new ArrayList<>();
//		String truncatedPath = path.substring(1);
//		List<Integer> indexes = IntStream.range(0, truncatedPath.length())
//			.filter(i -> truncatedPath.charAt(i) == '/')
//			.boxed()
//			.collect(Collectors.toList());
//		indexes.add(0);
//		indexes.sort(Comparator.naturalOrder());
//		List<String> out = new ArrayList<>();
//		for (int i = 0; i < indexes.size() - 1; i++) {
//			int from = indexes.get(i);
//			int to = indexes.get(i + 1)
//			out.add(truncatedPath.substring(from, to))
//		}
//	}
}
