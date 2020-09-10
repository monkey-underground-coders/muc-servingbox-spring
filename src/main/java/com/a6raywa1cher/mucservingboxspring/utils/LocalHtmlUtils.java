package com.a6raywa1cher.mucservingboxspring.utils;

import org.springframework.web.util.HtmlUtils;

public class LocalHtmlUtils {
	public static String htmlEscape(String input) {
		return input == null ? null : HtmlUtils.htmlEscape(input);
	}
}
