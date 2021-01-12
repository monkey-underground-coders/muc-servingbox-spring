package com.a6raywa1cher.mucservingboxspring.service.impl;

import com.a6raywa1cher.mucservingboxspring.service.DiskService;
import com.a6raywa1cher.mucservingboxspring.service.exc.ResourceBusyException;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * {@code BusyResource} controls access to the local path.
 *
 * <p>It prevents the situation when two or more users trying to make a critical
 * action on the same local path.
 * {@code BusyResource} acts like a resource and implements the {@link AutoCloseable} interface.
 * </p>
 * The typical usage is:
 * <pre> {@code
 * try (BusyResource ignored = new BusyResource(busyFiles, path)) {
 *     ...
 * }} </pre>
 *
 * <p>
 * Note that {@link #paths} is an external collection, holding all busy paths.
 * </p>
 */
class BusyResource implements AutoCloseable {
	private final Set<Path> paths;
	private final Path path;

	/**
	 * Creates an busy resource and trying to acquire control over it.
	 *
	 * @param paths collection of all busy paths.
	 * @param path  new local path.
	 * @throws ResourceBusyException when {@code path} is already in {@code paths}.
	 */
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


/**
 * {@code DiskServiceImpl} provides simple methods to work with the local file repository.
 *
 * <p>
 * On double access to the same local path, {@code DiskServiceImpl} denies second access by default.
 * Methods with read-only access only check if the local path isn't in use.
 * </p>
 *
 * <p>
 * Some methods work in a task mode: methods puts the task to {@link #executorService} and return nothing.
 * </p>
 *
 * @see com.a6raywa1cher.mucservingboxspring.service.DiskService
 */
@Service
@AllArgsConstructor
@Slf4j
public class DiskServiceImpl implements DiskService {
	/**
	 * The path to the local file repository.
	 */
	private final Path root;

	/**
	 * The {@link ExecutorService} for task mode methods.
	 */
	private final ExecutorService executorService;

	/**
	 * Collection of busy local paths. Access on any of this path will be prevented.
	 */
	private final Set<Path> busyFiles;

	/**
	 * Creates new {@code DiskServiceImpl}.
	 *
	 * @param root path to the local file repository.
	 */
	@Autowired
	public DiskServiceImpl(@Value("${app.upload-dir}") Path root) {
		this.root = root;
		this.executorService = new ForkJoinPool();
		this.busyFiles = new HashSet<>();
	}

	/**
	 * Checks if {@code path} exists on the disk, otherwise creates the folders on the path.
	 *
	 * @param path absolute path.
	 */
	private static void createDirs(Path path) {
		if (!path.toFile().exists() && !path.toFile().mkdirs()) {
			throw new RuntimeException("Unable to create dir " + path.toAbsolutePath().toString());
		}
	}

	/**
	 * Prepares {@code DiskServiceImpl} for work. Must be called before any method from this class.
	 *
	 * <p>
	 * Ensures that {@link #root} exists, otherwise tries to create the root folder.
	 * </p>
	 */
	@PostConstruct
	public void init() {
		createDirs(root);
	}

	/**
	 * Creates a new random, unique path to a new file in the repository.
	 * <p>
	 * Uses {@link UUID} to create a new path.
	 * </p>
	 * <p>
	 * The new path contains three inner folders and one file. For example:
	 * <pre>{@code 5d/39/31/9e8adf4eed891f1fc562ea6cad}</pre>
	 * This path structure causes less performance damage than the "all files in one folder" strategy.
	 * </p>
	 *
	 * @return random, unique, absolute path.
	 */
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

	/**
	 * Creates a new file with content from the {@code inputStream}.
	 *
	 * @param inputStream input stream with the content of a new file.
	 * @return pair of relative path and count of transferred bytes.
	 */
	@Override
	public Pair<Path, Long> createFile(InputStream inputStream) {
		Path path = newPath();
		long transferred = transferToPath(inputStream, path);
		if (transferred == -1) {
			return null;
		}
		return Pair.of(root.relativize(path), transferred);
	}

	/**
	 * Replaces the content from the {@code path} to the data from the {@code inputStream}.
	 * <p>
	 * Blocks the access to the {@code path}.
	 * </p>
	 *
	 * @param inputStream input stream with the new content of the file.
	 * @param path        absolute path to the file.
	 * @return transferred bytes or -1 on error.
	 */
	private long transferToPath(InputStream inputStream, Path path) {
		try (OutputStream outputStream = openOutputStream(path);
			 BusyResource ignored = new BusyResource(busyFiles, path)) {
			return inputStream.transferTo(outputStream);
		} catch (IOException e) {
			log.error(String.format("Error while the file transfer (nio), dest:%s", path), e);
			return -1;
		}
	}

	/**
	 * Creates a new file with content from the {@code multipartFile}.
	 *
	 * @param multipartFile container with the content for a new file.
	 * @return pair of relative path and count of transferred bytes or null on error.
	 */
	@Override
	public Pair<Path, Long> createFile(MultipartFile multipartFile) {
		Path path = newPath();
		long transferred = transferToPath(multipartFile, path);
		if (transferred == -1) {
			return null;
		}
		return Pair.of(root.relativize(path), transferred);
	}

	/**
	 * Replaces the content from the {@code path} to the data from the {@code multipartFile}.
	 * <p>
	 * Blocks the access to the {@code path}.
	 * </p>
	 *
	 * @param multipartFile container with the new content for the file.
	 * @param path          absolute path to the file.
	 * @return transferred bytes or -1 on errors.
	 */
	private long transferToPath(MultipartFile multipartFile, Path path) {
		try (BusyResource ignored = new BusyResource(busyFiles, path)) {
			multipartFile.transferTo(path);
			return multipartFile.getSize();
		} catch (IOException e) {
			log.error(String.format("Error while the file transfer (multipart), dest:%s", path), e);
			return -1;
		}
	}

	/**
	 * Replaces the content from the {@code path} to the data from the {@code inputStream}.
	 * <p>
	 * Blocks the access to the {@code path}.
	 * </p>
	 *
	 * @param path        relative path to the file.
	 * @param inputStream input stream with the new content of the file.
	 * @return transferred bytes or -1 on errors.
	 */
	@Override
	public Long modifyFile(Path path, InputStream inputStream) {
		return transferToPath(inputStream, root.resolve(path));
	}

	/**
	 * Replaces the content from the {@code path} to the data from the {@code multipartFile}.
	 * <p>
	 * Blocks the access to the {@code path}.
	 * </p>
	 *
	 * @param path relative path to the file.
	 * @param file container with the new content for the file.
	 * @return transferred bytes or -1 on errors.
	 */
	@Override
	public Long modifyFile(Path path, MultipartFile file) {
		return transferToPath(file, root.resolve(path));
	}

	/**
	 * Creates a new file with exact same content, as it was in the file from {@code path}.
	 * <p>
	 * Blocks the access to the {@code path}.
	 * </p>
	 *
	 * @param path relative path to copy.
	 * @return pair of relative path and count of transferred bytes or null on error.
	 */
	@Override
	public Pair<Path, Long> copyFile(Path path) {
		Path newPath = newPath();
		try (InputStream inputStream = openInputStream(path)) {
			long transferred = transferToPath(inputStream, newPath);
			return Pair.of(newPath, transferred);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Copies all files with paths from {@code paths}.
	 * <p>
	 * Blocks the access to all paths from {@code paths}.
	 * This method works in task mode.
	 * </p>
	 *
	 * @param paths relative paths to copy.
	 * @return map with triples "old path", "new path" and "transferred bytes".
	 * @implNote transferred bytes is always the expected number of transferred bytes.
	 */
	@Override
	@SneakyThrows(InterruptedException.class)
	public Map<Path, Pair<Path, Long>> copyFiles(List<Path> paths) {
		Map<Path, Pair<Path, Long>> out = paths.stream()
			.collect(Collectors.toMap(p -> p, p -> Pair.of(root.relativize(newPath()), root.resolve(p).toFile().length())));
		executorService.invokeAll(out.entrySet().stream()
			.map(e -> (Callable<Void>) () -> {
				Path oldPath = root.resolve(e.getKey());
				Path newPath = root.resolve(e.getValue().getFirst());
				long estimatedSize = e.getValue().getSecond();
				try (InputStream inputStream = openInputStream(oldPath)) {
					long transferred = transferToPath(inputStream, newPath);
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

	/**
	 * Transforms relative path to {@link File}.
	 * <p>
	 * Performs check if path isn't blocked.
	 * </p>
	 *
	 * @param path relative path
	 * @return {@link File} of the provided path
	 */
	@Override
	public File resolve(Path path) {
		Path absolutePath = root.resolve(path);
		throwIfLock(absolutePath);
		return absolutePath.toFile();
	}

	/**
	 * Transforms relative path to {@link Resource}.
	 * <p>
	 * Performs check if path isn't blocked.
	 * </p>
	 *
	 * @param path relative path.
	 * @return {@link Resource} of the provided path.
	 */
	@Override
	public Resource resolveAsResource(Path path) {
		Path absolutePath = root.resolve(path);
		throwIfLock(absolutePath);
		return new PathResource(absolutePath);
	}

	/**
	 * Deletes the file from {@code path}.
	 * <p>
	 * Blocks the access to the {@code path}.
	 * </p>
	 *
	 * @param path relative path.
	 */
	@Override
	public void deleteFile(Path path) {
		try (BusyResource ignored = new BusyResource(busyFiles, path)) {
			if (!resolve(path).delete()) {
				log.warn("Couldn't delete file {}", path);
			}
		}
	}

	/**
	 * Deletes all files with paths from {@code paths}.
	 * <p>
	 * Blocks the access to all paths from {@code paths}.
	 * This method works in task mode.
	 * </p>
	 *
	 * @param paths relative paths to delete.
	 */
	@Override
	@SneakyThrows(InterruptedException.class)
	public void deleteFiles(List<Path> paths) {
		executorService.invokeAll(paths.stream()
			.distinct()
			.map(p -> (Callable<Void>) () -> {
				deleteFile(p);
				return null;
			})
			.collect(Collectors.toList()));
	}

	/**
	 * Checks if the {@code path} isn't blocked, otherwise throws an exception.
	 *
	 * @param path absolute path to check.
	 * @throws ResourceBusyException when path is blocked.
	 */
	private void throwIfLock(Path path) {
		if (busyFiles.contains(path)) throw new ResourceBusyException();
	}

	/**
	 * Opens a new {@link InputStream} for the {@code path}.
	 *
	 * @param path absolute path to the file.
	 * @return new InputStream.
	 * @throws FileNotFoundException if path can't be resolved.
	 */
	private InputStream openInputStream(Path path) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(path.toFile()));
	}

	/**
	 * Opens a new {@link OutputStream} for the {@code path}.
	 *
	 * @param path absolute path to the file.
	 * @return new InputStream.
	 * @throws IOException if an i/o error occur.
	 */
	private OutputStream openOutputStream(Path path) throws IOException {
		return new BufferedOutputStream(new FileOutputStream(path.toFile()));
	}
}
