package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FSEntityService {
	FSEntity createNewHome(User creator);

	FSEntity createNewLessonRoot(LessonSchema lessonSchema);

	FSEntity createNewLiveLessonRoot(LiveLesson lessonSchema);

	FSEntity createNewFolder(FSEntity parent, String name, boolean hidden, User creator);

	FSEntity createNewFile(FSEntity parent, String name, MultipartFile file, boolean hidden, User creator);

	Resource getFileContent(FSEntity file);

	Optional<FSEntity> getById(Long id);

	Optional<FSEntity> getByPath(String path);

	FSEntity modifyFile(FSEntity file, MultipartFile newContent);

	FSEntity copyEntity(FSEntity object, FSEntity parent, String name,
						boolean hidden, User creator);

	FSEntity moveEntity(FSEntity object, FSEntity parent, String name,
						boolean hidden, User creator);

	void deleteEntity(FSEntity entity);
}
