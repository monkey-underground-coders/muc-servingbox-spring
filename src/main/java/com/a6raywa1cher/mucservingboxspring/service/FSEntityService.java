package com.a6raywa1cher.mucservingboxspring.service;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface FSEntityService {
	FSEntity createNewHome(User creator);

	FSEntity createNewHome(User creator, long maxSize);

	FSEntity createNewLessonRoot(LessonSchema lessonSchema);

	FSEntity createNewLiveLessonRoot(LiveLesson lessonSchema);

	FSEntity createNewFolder(FSEntity parent, String name, boolean hidden, User creator);

	FSEntity createNewFile(FSEntity parent, String name, MultipartFile file, boolean hidden, User creator);

	Resource getFileContent(FSEntity file);

	Optional<FSEntity> getById(Long id);

	Stream<FSEntity> getById(Collection<Long> ids);

	Optional<FSEntity> getByPath(String path);

	Optional<FSEntity> getParent(FSEntity child);

	long calculateSpaceLeft(String childPath);

	long calculateMaxSize(String childPath);

	FSEntity editMaxSize(FSEntity entity, long maxSize);

	FSEntity modifyFile(FSEntity file, MultipartFile newContent);

	FSEntity copyEntity(FSEntity object, FSEntity parent, String name,
						boolean hidden, User creator);

	FSEntity copyFolderContent(FSEntity object, FSEntity parent, boolean hidden, User creator);

	FSEntity moveEntity(FSEntity object, FSEntity parent, String name,
						boolean hidden, User creator);

	void deleteEntity(FSEntity entity);
}
