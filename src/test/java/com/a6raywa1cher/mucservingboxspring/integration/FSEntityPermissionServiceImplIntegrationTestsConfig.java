package com.a6raywa1cher.mucservingboxspring.integration;

import com.a6raywa1cher.mucservingboxspring.service.DiskService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class FSEntityPermissionServiceImplIntegrationTestsConfig {
	@Bean
	public DiskService diskService() {
		DiskService mock = mock(DiskService.class);
		when(mock.createFile(any(MultipartFile.class))).thenReturn(Pair.of(Path.of("/somewhere/somewhat"), 42L));
		return mock;
	}
}
