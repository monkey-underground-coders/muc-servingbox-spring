package com.a6raywa1cher.mucservingboxspring.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

@Configuration
@EnableSpringDataWebSupport
@EnableScheduling
@Slf4j
public class ApplicationConfig {
//	@PersistenceContext
//	EntityManager entityManager;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder();
	}

	@Bean
	public TaskExecutor executor() {
		return new ThreadPoolTaskExecutor();
	}

//	@EventListener(ApplicationStartedEvent.class)
//	@Transactional
//	public void createIndexes() throws SQLException {
//		log.info("DB name: {}", getDbName());
//		if ("postgresql".equals(getDbName())) {
//			Query nativeQuery = entityManager.createNativeQuery(
//				"create index if not exists idx_fts on public.lesson_schema using gin(to_tsvector('russian'::regconfig, description));"
//			);
//			nativeQuery.executeUpdate();
//		}
//	}
//
//	private String getDbName() throws SQLException {
//		SessionImplementor sessionImp = (SessionImplementor) entityManager.getDelegate();
//		DatabaseMetaData metadata = sessionImp.connection().getMetaData();
//		return metadata.getDatabaseProductName().toLowerCase();
//	}
}
