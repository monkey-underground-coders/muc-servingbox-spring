package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface UserService {
	User create(String registrationIp);

	User create(UserRole userRole, String username, String name, String password, String registrationIp);

	Optional<User> getById(Long id);

	Stream<User> getById(Collection<Long> ids);

	Optional<User> getByUsername(String username);

	User editUser(User user, UserRole userRole, String username, String name);

	User editPassword(User user, String password);

	User editRootFolder(User user, FSEntity root);

	User setLastVisitAt(User user, ZonedDateTime at);

	Optional<User> findFirstByUserRole(UserRole role);

	void deleteUser(User user);
}
