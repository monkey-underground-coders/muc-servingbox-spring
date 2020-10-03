package com.a6raywa1cher.mucservingboxspring.unit;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityRepository;
import com.a6raywa1cher.mucservingboxspring.service.DiskService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.impl.FSEntityServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FSEntityServiceImplUnitTests {
	@Mock
	private FSEntityRepository fsEntityRepository;
	@Mock
	private FSEntityPermissionService fsEntityPermissionService;
	@Mock
	private DiskService diskService;

	@Test
	public void copyFile() {
		FSEntityService service = new FSEntityServiceImpl(fsEntityRepository, fsEntityPermissionService, diskService);

		FSEntity targetFile = FSEntity.createFile("/f1/meow", "/12345", 1L, null, false);
		FSEntity parent2 = FSEntity.createFolder("/f2/", null, false);

		mockDiskService();
		List<FSEntity> saved = getSaveTracker();

		FSEntity output = service.copyEntity(targetFile, parent2, "meow2", false, null);

		assertEquals(1, saved.size());

		FSEntity toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals("/f2/meow2", toCheck.getPath());
		assertEquals(Path.of("/somewhere/somewhat"), Path.of(toCheck.getDiskObjectPath()));
		assertEquals(43L, toCheck.getByteSize());
		assertTrue(toCheck.isFile());
	}

	@Test
	public void copyFolder() {
		FSEntityService service = new FSEntityServiceImpl(fsEntityRepository, fsEntityPermissionService, diskService);

		FSEntity folder = FSEntity.createFolder("/f1/", null, false);
		FSEntity file = FSEntity.createFile("/f1/meow", "/12345", 1L, null, false);
		FSEntity parent2 = FSEntity.createFolder("/f2/", null, false);

		mockDiskService();
		List<FSEntity> saved = getSaveTracker();
		when(fsEntityRepository.getTreeByPath("/f1/"))
			.thenReturn(List.of(folder, file));

		FSEntity output = service.copyEntity(folder, parent2, "meow2", false, null);

		saved.sort(Comparator.comparing(FSEntity::getPath));
		assertEquals(2, saved.size());

		FSEntity toCheck1 = saved.get(0);
		assertEquals(toCheck1, output);
		assertEquals("/f2/meow2/", toCheck1.getPath());
		assertTrue(toCheck1.isFolder());

		FSEntity toCheck2 = saved.get(1);
		assertEquals("/f2/meow2/meow", toCheck2.getPath());
		assertTrue(toCheck2.isFile());
		assertEquals(1651L, toCheck2.getByteSize());
		assertEquals(Path.of("/12345/copy"), Path.of(toCheck2.getDiskObjectPath()));
	}


	@Test
	public void moveFile() {
		FSEntityService service = new FSEntityServiceImpl(fsEntityRepository, fsEntityPermissionService, diskService);

		FSEntity targetFile = FSEntity.createFile("/f1/meow", "/12345", 1L, null, false);
		FSEntity parent2 = FSEntity.createFolder("/f2/", null, false);

		List<FSEntity> saved = getSaveTracker();

		FSEntity output = service.moveEntity(targetFile, parent2, "meow2", false, null);

		assertEquals(1, saved.size());

		FSEntity toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals("/f2/meow2", toCheck.getPath());
		assertEquals(Path.of("/12345"), Path.of(toCheck.getDiskObjectPath()));
		assertEquals(1L, toCheck.getByteSize());
		assertTrue(toCheck.isFile());

		verify(fsEntityRepository).delete(any());
		verify(fsEntityRepository).delete(targetFile);
		verify(fsEntityPermissionService).deletePermissionsTreeFor(any());
		verify(fsEntityPermissionService).deletePermissionsTreeFor(targetFile);
	}

	@Test
	public void moveFolder() {
		FSEntityService service = new FSEntityServiceImpl(fsEntityRepository, fsEntityPermissionService, diskService);

		FSEntity folder = FSEntity.createFolder("/f1/", null, false);
		FSEntity file = FSEntity.createFile("/f1/meow", "/12345", 1L, null, false);
		FSEntity parent2 = FSEntity.createFolder("/f2/", null, false);

		when(fsEntityRepository.getTreeByPath("/f1/"))
			.thenReturn(List.of(folder, file));
		List<FSEntity> saved = getSaveTracker();

		FSEntity output = service.moveEntity(folder, parent2, "meow2", false, null);

		saved.sort(Comparator.comparing(FSEntity::getPath));
		assertEquals(2, saved.size());

		FSEntity toCheck1 = saved.get(0);
		assertEquals(toCheck1, output);
		assertEquals("/f2/meow2/", toCheck1.getPath());
		assertTrue(toCheck1.isFolder());

		FSEntity toCheck2 = saved.get(1);
		assertEquals("/f2/meow2/meow", toCheck2.getPath());
		assertEquals(Path.of("/12345"), Path.of(toCheck2.getDiskObjectPath()));
		assertEquals(1L, toCheck2.getByteSize());

		verify(fsEntityRepository).deleteAllTree(any());
		verify(fsEntityRepository).deleteAllTree("/f1/");
		verify(fsEntityPermissionService).deletePermissionsTreeFor(any());
		verify(fsEntityPermissionService).deletePermissionsTreeFor(folder);
	}

	@Test
	public void deleteFile() {
		FSEntityService service = new FSEntityServiceImpl(fsEntityRepository, fsEntityPermissionService, diskService);

		FSEntity file = FSEntity.createFile("/f1/meow", "/12345", 1L, null, false);

		service.deleteEntity(file);

		verify(fsEntityRepository).delete(file);
		verify(fsEntityPermissionService).deletePermissionsTreeFor(file);
		verify(diskService).deleteFile(Path.of("/12345"));
	}

	@Test
	public void deleteFolder() {
		FSEntityService service = new FSEntityServiceImpl(fsEntityRepository, fsEntityPermissionService, diskService);

		FSEntity folder = FSEntity.createFolder("/f1/", null, false);
		FSEntity file = FSEntity.createFile("/f1/meow", "/12345", 1L, null, false);

		when(fsEntityRepository.getFilesTreeByPath("/f1/"))
			.thenReturn(List.of(file));

		service.deleteEntity(folder);

		verify(fsEntityRepository).deleteAllTree(folder.getPath());
		verify(diskService).deleteFiles(List.of(Path.of("/12345")));
		verify(fsEntityPermissionService).deletePermissionsTreeFor(folder);
	}

	@Test
	public void createFile() {
		FSEntityService service = new FSEntityServiceImpl(fsEntityRepository, fsEntityPermissionService, diskService);

		FSEntity parent = FSEntity.createFolder("/f1/", null, false);
		MultipartFile multipartFile = mock(MultipartFile.class);
		User user = mock(User.class);

		List<FSEntity> saved = getSaveTracker();
		when(diskService.createFile(multipartFile))
			.thenReturn(Pair.of(Path.of("/file.txt"), 42L));

		FSEntity output = service.createNewFile(parent, "meow.txt", multipartFile, false, user);

		assertEquals(1, saved.size());

		FSEntity toCheck = saved.get(0);
		assertEquals(toCheck, output);
		assertEquals("/f1/meow.txt", toCheck.getPath());
		assertEquals(Path.of("/file.txt"), Path.of(toCheck.getDiskObjectPath()));
		assertEquals(42L, toCheck.getByteSize());
		assertEquals(false, toCheck.getHidden());
		assertEquals(user, toCheck.getCreatedBy());
		assertTrue(toCheck.isFile());

		verify(diskService).createFile(multipartFile);
	}

	private void mockDiskService() {
		when(diskService.copyFile(any()))
			.thenReturn(Pair.of(Path.of("/somewhere/somewhat"), 43L));
		when(diskService.copyFiles(anyList()))
			.then((inv) -> {
				List<Path> list = inv.getArgument(0);
				return list.stream()
					.collect(Collectors.toMap(p -> p, p -> Pair.of(p.resolve("copy"), 1651L)));
			});
	}

	private List<FSEntity> getSaveTracker() {
		List<FSEntity> saved = new ArrayList<>();
		when(fsEntityRepository.save(any()))
			.then((inv) -> {
				FSEntity e = inv.getArgument(0);
				saved.add(e);
				return e;
			});
		return saved;
	}
}
