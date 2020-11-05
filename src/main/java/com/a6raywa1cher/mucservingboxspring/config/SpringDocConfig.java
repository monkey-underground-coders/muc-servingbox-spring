package com.a6raywa1cher.mucservingboxspring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
	@Value("${app.version}")
	private String version;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info().title("MUC ServingBox").version(version).license(new License().name("MIT License").url("https://github.com/monkey-underground-coders/muc-servingbox-spring/blob/master/LICENSE")))
			.components(new Components()
				.addSecuritySchemes("basic",
					new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))
				.addSecuritySchemes("jwt",
					new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
	}
}
