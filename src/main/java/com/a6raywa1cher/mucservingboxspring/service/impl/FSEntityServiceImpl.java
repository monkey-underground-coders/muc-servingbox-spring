package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityRepository;
import com.a6raywa1cher.mucservingboxspring.service.DiskService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FSEntityServiceImpl implements FSEntityService {
	private final FSEntityRepository repository;
	private final FSEntityPermissionService permissionService;
	private final DiskService diskService;

	@Autowired
	public FSEntityServiceImpl(FSEntityRepository repository, FSEntityPermissionService permissionService, DiskService diskService) {
		this.repository = repository;
		this.permissionService = permissionService;
		this.diskService = diskService;
	}

	private FSEntity createNode(String virtualPath, boolean isFolder, String diskObjectPath,
								boolean hidden, long byteSize, User creator) {
		FSEntity fsEntity = new FSEntity();
		fsEntity.setPath(virtualPath);
		fsEntity.setIsFolder(isFolder);
		fsEntity.setDiskObjectPath(diskObjectPath);
		fsEntity.setHidden(hidden);
		fsEntity.setCreatedBy(creator);
		fsEntity.setCreatedTimestamp(ZonedDateTime.now());
		fsEntity.setModifiedTimestamp(ZonedDateTime.now());
		fsEntity.setByteSize(byteSize);
		return repository.save(fsEntity);
	}

	private void createFullPermissions(FSEntity fsEntity, User user) {
		permissionService.create(Collections.singletonList(fsEntity), Collections.singletonList(user),
			new ArrayList<>(), true, true, List.of(ActionType.values()));
	}

	@Override
	public FSEntity createNewHome(User creator) {
		FSEntity fsEntity = createNode('/' + creator.getId() + "/home/",
			true,
			null,
			false,
			0, creator);
		createFullPermissions(fsEntity, creator);
		return fsEntity;
	}

	@Override
	public FSEntity createNewLessonRoot(LessonSchema lessonSchema) {
		User creator = lessonSchema.getCreator();
		return createNode('/' + creator.getId() + "/lroot/" + lessonSchema.getId() + '/',
			true,
			null,
			false,
			0, creator);
	}

	@Override
	public FSEntity createNewLiveLessonRoot(LiveLesson liveLesson) {
		LessonSchema lessonSchema = liveLesson.getSchema();
		User creator = lessonSchema.getCreator();
		return createNode('/' + creator.getId() + "/lroots/" +
				lessonSchema.getId() + '/' + liveLesson.getId() + '/',
			true,
			null,
			false,
			0, creator);
	}

	@Override
	public FSEntity createNewFolder(FSEntity parent, String name, boolean hidden, User creator) {
		return createNode(parent.getPath() + name + '/',
			true,
			null,
			hidden,
			0, creator);
	}

	@Override
	public FSEntity createNewFile(FSEntity parent, String name, MultipartFile file, boolean hidden, User creator) {
		Pair<Path, Long> pair = diskService.createFile(file);
		if (pair == null) {
			return null;
		}
		Path diskObjectPath = pair.getFirst();
		long size = pair.getSecond();
		return createNode(parent.getPath() + name,
			false,
			diskObjectPath.toString(),
			hidden,
			size, creator);
	}

	@Override
	public Resource getFileContent(FSEntity file) {
		String path = file.getDiskObjectPath();
		return diskService.resolveAsResource(Path.of(path));
	}

	@Override
	public Optional<FSEntity> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Optional<FSEntity> getByPath(String path) {
		return repository.findByPath(path);
	}

	@Override
	public FSEntity modifyFile(FSEntity file, MultipartFile newContent) {
		long size = diskService.modifyFile(Path.of(file.getPath()), newContent);
		if (size == -1) {
			return null;
		}
		file.setByteSize(size);
		file.setModifiedTimestamp(ZonedDateTime.now());
		return repository.save(file);
	}

	@Override
	public FSEntity copyEntity(FSEntity object, FSEntity parent, String name,
							   boolean hidden, User creator) {
		if (object.isFile()) {
			Pair<Path, Long> pair = diskService.copyFile(Path.of(object.getPath()));
			if (pair == null) {
				return null;
			}
			Path newFile = pair.getFirst();
			long size = pair.getSecond();
			return createNode(
				parent.getPath() + name,
				false,
				newFile.toString(),
				hidden,
				size,
				creator
			);
		} else {
			URI originalParent = URI.create(object.getPath());
			URI targetParent = URI.create(parent.getPath());
			List<FSEntity> fsEntities = permissionService.getAllChildrenWithAccess(object, creator, null, ActionType.READ);
			List<FSEntity> files = fsEntities.stream().filter(FSEntity::isFile).collect(Collectors.toList());
			Map<Path, Pair<Path, Long>> prevPathToNewPathAndSize =
				diskService.copyFiles(files.stream().map(FSEntity::getPath).map(Path::of).collect(Collectors.toList()));
			if (prevPathToNewPathAndSize == null) {
				return null;
			}
			fsEntities.stream()
				.forEach(entity -> {
					URI newPath = targetParent.resolve(originalParent.relativize(URI.create(entity.getPath())));
					Pair<Path, Long> pair = entity.isFile() ?
						prevPathToNewPathAndSize.get(Path.of(entity.getDiskObjectPath())) :
						null;
					createNode(newPath.toString(),
						entity.isFolder(),
						pair != null ? pair.getFirst().toString() : null,
						entity.getHidden(),
						pair != null ? pair.getSecond() : 0,
						entity.getCreatedBy());
				});
			return parent;
		}
	}

	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public void deleteFile(FSEntity entity) {
		if (entity.isFile()) {
			diskService.deleteFile(Path.of(entity.getDiskObjectPath()));
			permissionService.delete(permissionService.getByFSEntity(entity));
			repository.delete(entity);
		} else {
			diskService.deleteFiles(repository.findChildrenFilesByPath(entity.getPath()).parallelStream()
				.map(s -> Path.of(s.getDiskObjectPath()))
				.collect(Collectors.toList()));
			permissionService.deletePermissionsTreeFor(entity);
			repository.deleteAllChildren(entity.getPath());
		}
	}
}
