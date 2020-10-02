package com.a6raywa1cher.mucservingboxspring.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionImplementor;
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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Configuration
@EnableSpringDataWebSupport
@Slf4j
public class ApplicationConfig {
	@PersistenceContext
	EntityManager entityManager;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder();
	}

	@EventListener(ApplicationStartedEvent.class)
	@Transactional
	public void createIndexes() throws SQLException {
		log.info("DB name: {}", getDbName());
		if ("postgresql".equals(getDbName())) {
			Query nativeQuery = entityManager.createNativeQuery(
				"create index if not exists path_length_index on fsentity_permission_entities ((length(entities_path)) desc);" +
					"create index if not exists path_to_path_length_index on fsentity_permission_entities (entities_path, (length(entities_path)) desc);"
			);
			nativeQuery.executeUpdate();
		}
	}

	private String getDbName() throws SQLException {
		SessionImplementor sessionImp = (SessionImplementor) entityManager.getDelegate();
		DatabaseMetaData metadata = sessionImp.connection().getMetaData();
		return metadata.getDatabaseProductName().toLowerCase();
	}
}
