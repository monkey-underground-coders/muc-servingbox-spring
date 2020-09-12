package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.service.DiskService;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiskServiceImpl implements DiskService {
	private final Path root;
	private final ForkJoinPool pool;

	public DiskServiceImpl(@Value("${app.upload-dir}") Path root) {
		this.root = root;
		this.pool = new ForkJoinPool();
	}

	private static void createDirs(Path path) {
		if (!path.toFile().mkdirs()) {
			throw new RuntimeException("Unable to create dir");
		}
	}

	@PostConstruct
	public void init() {
		createDirs(root);
	}

	private Path newPath() {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		String[] parts = Iterables.toArray(Splitter.fixedLength(2).split(uuid), String.class);
		Path path = Arrays.stream(parts)
			.limit(3)
			.map(s -> Path.of(s))
			.reduce(root, Path::resolve);
		createDirs(path);
		return path
			.resolve(uuid.substring(6));
	}

	@Override
	public Pair<Path, Long> createFile(InputStream inputStream) {
		Path path = newPath();
		long transferred = transferToPath(inputStream, path);
		if (transferred == -1) {
			return null;
		}
		return Pair.of(root.relativize(path), transferred);
	}

	private long transferToPath(InputStream inputStream, Path path) {
		try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
			return inputStream.transferTo(outputStream);
		} catch (IOException e) {
			log.error(String.format("Error while the file transfer (nio), dest:%s", path), e);
			return -1;
		}
	}

	@Override
	public Pair<Path, Long> createFile(MultipartFile multipartFile) {
		Path path = newPath();
		long transferred = transferToPath(multipartFile, path);
		if (transferred == -1) {
			return null;
		}
		return Pair.of(root.relativize(path), transferred);
	}

	private long transferToPath(MultipartFile multipartFile, Path path) {
		try {
			multipartFile.transferTo(path);
			return multipartFile.getSize();
		} catch (IOException e) {
			log.error(String.format("Error while the file transfer (multipart), dest:%s", path), e);
			return -1;
		}
	}

	@Override
	public Long modifyFile(Path path, InputStream inputStream) {
		return transferToPath(inputStream, path);
	}

	@Override
	public Long modifyFile(Path path, MultipartFile file) {
		return transferToPath(file, path);
	}

	@Override
	public File resolve(Path path) {
		return root.resolve(path).toFile();
	}

	@Override
	public Resource resolveAsResource(Path path) {
		return new PathResource(resolve(path).toPath());
	}

	@Override
	public void deleteFile(Path path) {
		if (!resolve(path).delete()) {
			log.warn("Couldn't delete file {}", path);
		}
	}

	@Override
	public void deleteFiles(List<Path> files) {
		pool.invokeAll(files.stream()
			.distinct()
			.map(p -> (Callable<Void>) () -> {
				deleteFile(p);
				return null;
			})
			.collect(Collectors.toList()));
	}
}
