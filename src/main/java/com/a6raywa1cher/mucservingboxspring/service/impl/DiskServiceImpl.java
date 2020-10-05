package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.service.DiskService;
import com.a6raywa1cher.mucservingboxspring.service.exc.ResourceBusyException;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

class BusyResource implements AutoCloseable {
	private final Set<Path> paths;
	private final Path path;

	BusyResource(Set<Path> paths, Path path) throws ResourceBusyException {
		this.paths = paths;
		this.path = path;
		if (paths.contains(path)) throw new ResourceBusyException();
		paths.add(path);
	}

	@Override
	public void close() {
		paths.remove(path);
	}
}

@Service
@Slf4j
public class DiskServiceImpl implements DiskService {
	private final Path root;
	private final ForkJoinPool pool;
	private final Set<Path> busyFiles;

	public DiskServiceImpl(@Value("${app.upload-dir}") Path root) {
		this.root = root;
		this.pool = new ForkJoinPool();
		this.busyFiles = new HashSet<>();
	}

	private static void createDirs(Path path) {
		if (!path.toFile().exists() && !path.toFile().mkdirs()) {
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
			.map(Path::of)
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
		try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()));
			 BusyResource ignored = new BusyResource(busyFiles, path)) {
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
		try (BusyResource ignored = new BusyResource(busyFiles, path)) {
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
	public Pair<Path, Long> copyFile(Path path) {
		Path newPath = newPath();
		try {
			long transferred = transferToPath(new BufferedInputStream(new FileInputStream(path.toFile())), newPath);
			return Pair.of(newPath, transferred);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@Override
	public Map<Path, Pair<Path, Long>> copyFiles(List<Path> path) {
		Map<Path, Pair<Path, Long>> out = path.stream()
			.collect(Collectors.toMap(p -> p, p -> Pair.of(newPath(), p.toFile().length())));
		pool.invokeAll(out.entrySet().stream()
			.map(e -> (Callable<Void>) () -> {
				Path oldPath = e.getKey();
				Path newPath = e.getValue().getFirst();
				long estimatedSize = e.getValue().getSecond();
				try {
					long transferred = transferToPath(new BufferedInputStream(new FileInputStream(oldPath.toFile())), newPath);
					if (transferred != estimatedSize) {
						log.warn("During copy, transferred size {} didn't matched estimatedSize {}", transferred, estimatedSize);
					}
				} catch (FileNotFoundException e1) {
					log.warn("File not found during copy", e1);
				}
				return null;
			})
			.collect(Collectors.toList()));
		return out;
	}

	@Override
	public File resolve(Path path) {
		throwIfLock(path);
		return root.resolve(path).toFile();
	}

	@Override
	public Resource resolveAsResource(Path path) {
		throwIfLock(path);
		return new PathResource(resolve(path).toPath());
	}

	@Override
	public void deleteFile(Path path) {
		try (BusyResource ignored = new BusyResource(busyFiles, path)) {
			if (!resolve(path).delete()) {
				log.warn("Couldn't delete file {}", path);
			}
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

	private void throwIfLock(Path path) {
		if (busyFiles.contains(path)) throw new ResourceBusyException();
	}
}
