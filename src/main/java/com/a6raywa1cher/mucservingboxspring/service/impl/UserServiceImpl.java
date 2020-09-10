package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.repo.UserRepository;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository repository;

	@Autowired
	public UserServiceImpl(UserRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<User> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Optional<User> getByUsername(String username) {
		return repository.findByUsername(username);
	}

	@Override
	public User setLastVisitAt(User user, ZonedDateTime at) {
		user.setLastVisitAt(at);
		return repository.save(user);
	}

	@Override
	public Optional<User> findFirstByUserRole(UserRole role) {
		return repository.findFirstByUserRole(role);
	}
}
