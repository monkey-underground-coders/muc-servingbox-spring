package com.a6raywa1cher.mucservingboxspring.config;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

@Configuration
@EnableSpringDataWebSupport
public class ApplicationConfig {
	@PersistenceContext
	EntityManager entityManager;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder();
	}

	@EventListener(ApplicationStartedEvent.class)
	@Transactional
	public void createIndexes() {
		Query nativeQuery = entityManager.createNativeQuery(
			"create index if not exists path_length_index on fsentity_permission_entities ((length(entities_path)) desc);" +
				"create index if not exists path_to_path_length_index on fsentity_permission_entities (entities_path, (length(entities_path)) desc);"
		);
		nativeQuery.executeUpdate();
	}
}
