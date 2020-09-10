package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface UserService {
	Optional<User> getById(Long id);

	Optional<User> getByUsername(String username);

	User setLastVisitAt(User user, ZonedDateTime at);

	Optional<User> findFirstByUserRole(UserRole role);
}
