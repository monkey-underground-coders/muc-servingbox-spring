package com.a6raywa1cher.mucservingboxspring.service;

import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface DiskService {
	Pair<Path, Long> createFile(InputStream inputStream);

	Pair<Path, Long> createFile(MultipartFile inputStream);

	Long modifyFile(Path path, InputStream inputStream);

	Long modifyFile(Path path, MultipartFile inputStream);

	Pair<Path, Long> copyFile(Path path);

	File resolve(Path path);

	Resource resolveAsResource(Path path);

	void deleteFile(Path path);

	void deleteFiles(List<Path> files);
}
