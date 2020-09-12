package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface UserService {
	User create(String registrationIp);

	User create(UserRole userRole, String username, String name, String password, String registrationIp);

	Optional<User> getById(Long id);

	Optional<User> getByUsername(String username);

	User setLastVisitAt(User user, ZonedDateTime at);

	Optional<User> findFirstByUserRole(UserRole role);
}
