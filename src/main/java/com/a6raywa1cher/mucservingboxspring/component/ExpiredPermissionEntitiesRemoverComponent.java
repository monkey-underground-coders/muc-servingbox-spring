package com.a6raywa1cher.mucservingboxspring.component;

import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.repo.LiveLessonRepository;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
public class ExpiredPermissionEntitiesRemoverComponent {
	private final LiveLessonRepository liveLessonRepository;
	private final FSEntityPermissionService permissionService;

	public ExpiredPermissionEntitiesRemoverComponent(LiveLessonRepository liveLessonRepository, FSEntityPermissionService permissionService) {
		this.liveLessonRepository = liveLessonRepository;
		this.permissionService = permissionService;
	}

	@EventListener(ApplicationStartedEvent.class)
	@Transactional
	public void appStarted() {
		removeExpiredPermissions();
	}

	@Scheduled(fixedRateString = "${app.expired-remover-rate}")
	@Transactional
	public void removeExpiredPermissions() {
		for (LiveLesson lesson : liveLessonRepository.findExpired(ZonedDateTime.now())) {
			try {
				List<FSEntityPermission> managedStudentPermissions = lesson.getManagedStudentPermissions();
				permissionService.delete(managedStudentPermissions);
				log.info("Removed {} entities from liveLesson {}", managedStudentPermissions.size(), lesson.getId());
			} catch (Exception e) {
				log.error("Exception during liveLesson " + lesson.getId(), e);
			}
		}
	}
}
