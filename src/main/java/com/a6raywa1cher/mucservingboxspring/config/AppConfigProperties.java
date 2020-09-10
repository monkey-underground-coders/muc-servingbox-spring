package com.a6raywa1cher.mucservingboxspring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;

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

	@Data
	public static final class FirstAdmin {
		@NotBlank
		private String email;

		@NotBlank
		private String name;

		@NotBlank
		private String password;
	}
}
