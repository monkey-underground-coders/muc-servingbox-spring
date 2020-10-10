package com.a6raywa1cher.mucservingboxspring.config;

import com.a6raywa1cher.mucservingboxspring.utils.resolver.UserHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Valid;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final AppConfigProperties properties;
	private final UserHandlerMethodArgumentResolver userHandlerMethodArgumentResolver;

	@Autowired
	public WebConfig(@Valid AppConfigProperties properties, UserHandlerMethodArgumentResolver userHandlerMethodArgumentResolver) {
		this.properties = properties;
		this.userHandlerMethodArgumentResolver = userHandlerMethodArgumentResolver;
	}


	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html")
			.addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**")
			.addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(userHandlerMethodArgumentResolver);
	}
}