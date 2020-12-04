package com.a6raywa1cher.mucservingboxspring.model;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserRole {
	TEMPORARY_USER, STUDENT, TEACHER, ADMIN(true, TEMPORARY_USER, STUDENT, TEACHER);
	public final Set<UserRole> access;

	UserRole(UserRole... accessTo) {
		this(false, accessTo);
	}

	UserRole(boolean addSelf, UserRole... accessTo) {
		if (addSelf) {
			this.access =
				Stream.concat(Stream.of(accessTo), Stream.of(this)).collect(Collectors.toUnmodifiableSet());
		} else {
			this.access =
				Stream.of(accessTo).collect(Collectors.toUnmodifiableSet());
		}
	}
}
