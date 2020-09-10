package com.a6raywa1cher.mucservingboxspring.component;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class CreateFirstAdminApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
	private static final Logger logger = LoggerFactory.getLogger(CreateFirstAdminApplicationListener.class);

	private final String username;

	private final String name;

	private final String password;

	private final UserService userService;

	public CreateFirstAdminApplicationListener(@Value("${app.first-admin.username}") String username,
											   @Value("${app.first-admin.name}") String name,
											   @Value("${app.first-admin.password}") String password,
											   UserService userService) {
		this.username = username;
		this.name = name;
		this.password = password;
		this.userService = userService;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		if (userService.findFirstByUserRole(UserRole.ADMIN).isEmpty()) {
			userService.create(UserRole.ADMIN, username, name, password, "localhost");
			logger.info("Created admin-user");
		}
	}
}
