package com.a6raywa1cher.mucservingboxspring.utils;

import com.a6raywa1cher.mucservingboxspring.model.User;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
	private final AuthenticationResolver resolver;

	public UserHandlerMethodArgumentResolver(AuthenticationResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(User.class);
	}

	@Override
	@Transactional
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return resolver.getUser();
	}
}
