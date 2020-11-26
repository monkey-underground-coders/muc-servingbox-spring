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
import com.a6raywa1cher.mucservingboxspring.utils.AlgorithmUtils;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
								boolean hidden, long byteSize, long maxSize, User creator) {
		return repository.save(createNodeNoSave(virtualPath, isFolder, diskObjectPath, hidden, byteSize, maxSize, creator));
	}

	private static URI resolveBetweenParents(URI originalParent, URI targetParent, URI entityUri) {
		return targetParent.resolve(originalParent.relativize(entityUri));
	}

	private FSEntity createNodeNoSave(String virtualPath, boolean isFolder, String diskObjectPath,
									  boolean hidden, long byteSize, long maxSize, User creator) {
		FSEntity fsEntity = new FSEntity();
		fsEntity.setPath(virtualPath);
		int sepsCount = (int) AlgorithmUtils.count(virtualPath, '/');
		fsEntity.setPathLevel(isFolder ? sepsCount : sepsCount + 1);
		fsEntity.setIsFolder(isFolder);
		fsEntity.setDiskObjectPath(diskObjectPath);
		fsEntity.setHidden(hidden);
		fsEntity.setCreatedBy(creator);
		fsEntity.setCreatedTimestamp(ZonedDateTime.now());
		fsEntity.setModifiedTimestamp(ZonedDateTime.now());
		fsEntity.setByteSize(byteSize);
		fsEntity.setMaxSize(maxSize);
		return fsEntity;
	}

	private void createFullPermissions(FSEntity fsEntity, User user) {
		permissionService.create(fsEntity, Collections.singletonList(user),
			new ArrayList<>(), true, List.of(ActionType.values()));
	}

	@Override
	public FSEntity createNewLessonRoot(LessonSchema lessonSchema) {
		User creator = lessonSchema.getCreator();
		return createNode("/" + creator.getId() + "/lroot/" + lessonSchema.getId() + "/root/",
			true,
			null,
			false,
			0, -1, creator);
	}

	@Override
	public FSEntity createNewLiveLessonRoot(LiveLesson liveLesson) {
		LessonSchema lessonSchema = liveLesson.getSchema();
		User creator = lessonSchema.getCreator();
		return createNode("/" + creator.getId() + "/lroot/" +
				lessonSchema.getId() + '/' + liveLesson.getId() + '/',
			true,
			null,
			false,
			0, -1, creator);
	}

	@Override
	public FSEntity createNewFolder(FSEntity parent, String name, boolean hidden, User creator) {
		return createNode(parent.getPath() + name + '/',
			true,
			null,
			hidden,
			0, -1, creator);
	}

	@Override
	public long calculateSpaceLeft(String childPath) {
		List<String> parentPaths = IntStream.range(1, childPath.length())
			.filter(i -> childPath.charAt(i) == '/')
			.mapToObj(i -> childPath.substring(0, i + 1))
			.collect(Collectors.toList());
		List<FSEntity> parents = repository.findAllByPaths(parentPaths);
		parents.sort(Comparator.comparing(f -> f.getPath().length(), Comparator.reverseOrder()));
		return parents.stream()
			.map(p -> {
				if (p.getMaxSize() <= -1) return Long.MAX_VALUE;
				Long consumed = repository.countSubtreeFileSize(childPath);
				if (consumed == null) return p.getMaxSize();
				return Math.max(0, p.getMaxSize() - consumed);
			})
			.min(Comparator.naturalOrder()).orElse(Long.MAX_VALUE);
	}

	@Override
	public long calculateMaxSize(String childPath) {
		List<String> parentPaths = IntStream.range(1, childPath.length())
			.filter(i -> childPath.charAt(i) == '/')
			.mapToObj(i -> childPath.substring(0, i + 1))
			.collect(Collectors.toList());
		List<FSEntity> parents = repository.findAllByPaths(parentPaths);
		long maxSize = parents.stream()
			.map(p -> {
				if (p.getMaxSize() <= -1) return Long.MAX_VALUE;
				return p.getMaxSize();
			})
			.min(Comparator.naturalOrder()).orElse(Long.MAX_VALUE);
		return maxSize == Long.MAX_VALUE ? -1 : maxSize;
	}

	@Override
	public FSEntity editMaxSize(FSEntity entity, long maxSize) {
		entity.setMaxSize(maxSize);
		return repository.save(entity);
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
			size, -1, creator);
	}

	@Override
	public Resource getFileContent(FSEntity file) {
		String path = file.getDiskObjectPath();
		return diskService.resolveAsResource(Path.of(path));
	}

	@Override
	public Optional<FSEntity> getById(Long id) {
		return repository.getById(id);
	}

	@Override
	public Stream<FSEntity> getById(Collection<Long> ids) {
		return StreamSupport.stream(repository.findAllById(ids).spliterator(), false);
	}

	@Override
	public Optional<FSEntity> getByPath(String path) {
		return repository.findByPath(path).findAny();
	}

	@Override
	public Optional<FSEntity> getParent(FSEntity child) {
		String childPath = child.getPath();
		return this.getByPath(childPath.substring(0, childPath.lastIndexOf('/')));
	}

	@Override
	public FSEntity modifyFile(FSEntity file, MultipartFile newContent) {
		long size = diskService.modifyFile(Path.of(file.getDiskObjectPath()), newContent);
		if (size == -1) {
			return null;
		}
		file.setByteSize(size);
		file.setModifiedTimestamp(ZonedDateTime.now());
		return repository.save(file);
	}

	@Override
	public FSEntity createNewHome(User creator) {
		return this.createNewHome(creator, -1);
	}

	@Override
	public FSEntity createNewHome(User creator, long maxSize) {
		FSEntity fsEntity = createNode("/user_home/" + creator.getId() + '/',
			true,
			null,
			false,
			0, maxSize, creator);
		createFullPermissions(fsEntity, creator);
		return fsEntity;
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
				-1,
				creator
			);
		} else {
			URI originalParent = URI.create(object.getPath());
			URI targetParent = name != null ? URI.create(parent.getPath()).resolve(name + '/') : URI.create(parent.getPath());
			List<FSEntity> fsEntities = repository.getTreeByPath(object.getPath());
			List<FSEntity> files = fsEntities.stream().filter(FSEntity::isFile).collect(Collectors.toList());
			Map<Path, Pair<Path, Long>> prevPathToNewPathAndSize =
				diskService.copyFiles(files.stream().map(FSEntity::getDiskObjectPath).map(Path::of).collect(Collectors.toList()));
			if (prevPathToNewPathAndSize == null) {
				return null;
			}
			AtomicReference<FSEntity> out = new AtomicReference<>();
			fsEntities.forEach(entity -> {
				URI newPath = resolveBetweenParents(originalParent, targetParent, URI.create(entity.getPath()));
				Pair<Path, Long> pair = entity.isFile() ?
					prevPathToNewPathAndSize.get(Path.of(entity.getDiskObjectPath())) :
					null;
				FSEntity node = createNode(newPath.toString(),
					entity.isFolder(),
					pair != null ? pair.getFirst().toString() : null,
					entity.getHidden(),
					pair != null ? pair.getSecond() : 0,
					-1,
					creator);
				if (entity.getPath().equals(object.getPath())) {
					out.set(node);
				}
			});
			return out.get();
		}
	}

	@Override
	public FSEntity copyFolderContent(FSEntity object, FSEntity parent, boolean hidden, User creator) {
		return copyEntity(object, parent, null, hidden, creator);
	}

	@Override
	public FSEntity moveEntity(FSEntity object, FSEntity parent, String name, boolean hidden, User creator) {
		if (object.isFile()) {
			String newVirtualPath = parent.getPath() + name;
			FSEntity newEntity = createNode(newVirtualPath,
				false,
				object.getDiskObjectPath(),
				hidden,
				object.getByteSize(),
				-1,
				creator);
			repository.delete(object);
			permissionService.deletePermissionsTreeFor(object);
			return newEntity;
		} else {
			URI originalParent = URI.create(object.getPath());
			URI targetParent = URI.create(parent.getPath()).resolve(name + '/');
			List<FSEntity> fsEntities = repository.getTreeByPath(object.getPath());
			AtomicReference<FSEntity> out = new AtomicReference<>();
			fsEntities.forEach(entity -> {
				URI newPath = resolveBetweenParents(originalParent, targetParent, URI.create(entity.getPath()));
				FSEntity node = createNode(newPath.getPath(),
					entity.isFolder(),
					entity.getDiskObjectPath(),
					entity.getHidden(),
					entity.getByteSize(),
					entity.getMaxSize(),
					creator);
				if (entity.getPath().equals(object.getPath())) {
					out.set(node);
				}
			});
//			repository.deleteAllTree(object.getPath());
			repository.deleteAll(repository.getTreeByPath(object.getPath()));
			permissionService.deletePermissionsTreeFor(object);
			return out.get();
		}
	}

	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public void deleteEntity(FSEntity entity) {
		if (entity.isFile()) {
			diskService.deleteFile(Path.of(entity.getDiskObjectPath()));
			permissionService.deletePermissionsTreeFor(entity);
			repository.delete(entity);
		} else {
			diskService.deleteFiles(repository.getFilesTreeByPath(entity.getPath()).stream()
				.map(s -> Path.of(s.getDiskObjectPath()))
				.collect(Collectors.toList()));
			permissionService.deletePermissionsTreeFor(entity);
			repository.deleteAll(repository.getTreeByPath(entity.getPath()));
		}
	}
}
