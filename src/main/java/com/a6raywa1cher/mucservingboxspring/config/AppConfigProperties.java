package com.a6raywa1cher.mucservingboxspring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app")
@Validated
@Data
public class AppConfigProperties {
	@NotNull
	private String[] corsAllowedOrigins;

	@NotNull
	@Valid
	private FirstAdmin firstAdmin;

	@NotNull
	private Path uploadDir;

	@NotNull
	private Duration temporaryUserAccessDuration;

	@NotBlank
	private String temporaryUserName;

	@Data
	public static final class FirstAdmin {
		@NotBlank
		private String username;

		@NotBlank
		private String name;

		@NotBlank
		private String password;
	}
}
