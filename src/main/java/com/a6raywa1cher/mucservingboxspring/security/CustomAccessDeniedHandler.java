package com.a6raywa1cher.mucservingboxspring.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
//		if (accessDeniedException instanceof BadCredentialsException || accessDeniedException instanceof DisabledException) {
//			response.sendError(HttpServletResponse.SC_FORBIDDEN, authException.getMessage());
//		}
		int a = 0;
	}
}
