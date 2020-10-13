package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.repo.UserRepository;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final Duration temporaryUserAccessDuration;
	private final String temporaryUserName;

	@Autowired
	public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder,
						   @Value("${app.temporary-user-access-duration}") Duration temporaryUserAccessDuration,
						   @Value("${app.temporary-user-name}") String temporaryUserName) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.temporaryUserAccessDuration = temporaryUserAccessDuration;
		this.temporaryUserName = temporaryUserName;
	}

	private int extractNumber(String name) {
		return Integer.parseInt(name.substring(name.lastIndexOf('#') + 1));
	}

	@Override
	public synchronized User create(String registrationIp) {
		Optional<User> lastTemporaryUser = repository.findTop1ByUserRole(
			UserRole.TEMPORARY_USER,
			PageRequest.of(0, 1, Sort.Direction.DESC, "id"))
			.stream().findFirst();
		int number = lastTemporaryUser.map(user -> extractNumber(user.getName())).orElse(1);
		return create(UserRole.TEMPORARY_USER,
			UUID.randomUUID().toString(),
			String.format(temporaryUserName, number),
			UUID.randomUUID().toString(),
			registrationIp);
	}

	@Override
	public synchronized User create(UserRole userRole, String username, String name, String password, String registrationIp) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setName(name);
		user.setUserRole(userRole);
		user.setSchemaList(new ArrayList<>());
		user.setExpiringAt(userRole == UserRole.TEMPORARY_USER ?
			ZonedDateTime.now().plus(temporaryUserAccessDuration) :
			null);
		user.setCreatedAt(ZonedDateTime.now());
		user.setCreatedIp(registrationIp);
		user.setLastVisitAt(ZonedDateTime.now());
		return repository.save(user);
	}

	@Override
	public Optional<User> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Stream<User> getById(Collection<Long> ids) {
		return StreamSupport.stream(repository.findAllById(ids).spliterator(), false);
	}

	@Override
	public Optional<User> getByUsername(String username) {
		return repository.findByUsername(username);
	}

	@Override
	public User editUser(User user, UserRole userRole, String username, String name) {
		user.setUsername(username);
		user.setName(name);
		return repository.save(user);
	}

	@Override
	public User editPassword(User user, String password) {
		user.setPassword(passwordEncoder.encode(password));
		return repository.save(user);
	}

	@Override
	public User editRootFolder(User user, FSEntity root) {
		user.setRootFolder(root);
		return repository.save(user);
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

	@Override
	public void deleteUser(User user) {
		repository.delete(user);
	}
}
