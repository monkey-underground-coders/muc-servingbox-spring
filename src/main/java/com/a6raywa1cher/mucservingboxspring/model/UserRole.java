package com.a6raywa1cher.mucservingboxspring.model;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserRole {
	TEMPORARY_USER, STUDENT, TEACHER, ADMIN(TEMPORARY_USER, STUDENT, TEACHER);
	public final Set<UserRole> access;

	UserRole(UserRole... accessTo) {
		this.access =
			Stream.of(accessTo).collect(Collectors.toUnmodifiableSet());
	}
}
