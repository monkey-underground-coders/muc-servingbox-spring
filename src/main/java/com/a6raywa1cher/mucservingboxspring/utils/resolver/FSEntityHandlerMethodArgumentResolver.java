//package com.a6raywa1cher.mucservingboxspring.utils.resolver;
//
//import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
//import com.a6raywa1cher.mucservingboxspring.model.repo.FSEntityRepository;
//import org.springframework.core.MethodParameter;
//import org.springframework.web.bind.support.WebDataBinderFactory;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.ModelAndViewContainer;
//
//public class FSEntityHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
//	private final FSEntityRepository repository;
//
//	public FSEntityHandlerMethodArgumentResolver(FSEntityRepository repository) {
//		this.repository = repository;
//	}
//
//	@Override
//	public boolean supportsParameter(MethodParameter parameter) {
//		return parameter.getParameterType().equals(FSEntity.class) && parameter.getMethodAnnotation(FSEntityInject.class) != null;
//	}
//
//	@Override
//	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//
//	}
//}
