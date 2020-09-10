package com.a6raywa1cher.mucservingboxspring.security.jwt;

import com.a6raywa1cher.mucservingboxspring.security.authentication.JwtAuthentication;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private final JwtTokenService jwtTokenService;
	private final AuthenticationManager authenticationManager;

	public JwtAuthenticationFilter(JwtTokenService jwtTokenService, AuthenticationManager authenticationManager) {
		this.jwtTokenService = jwtTokenService;
		this.authenticationManager = authenticationManager;
	}

	private Authentication check(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		if (request.getHeader(AUTHORIZATION_HEADER) == null) {
			return null;
		}
		String lowerCase = request.getHeader(AUTHORIZATION_HEADER).toLowerCase();
		String token;
		if (lowerCase.startsWith("jwt ") || lowerCase.startsWith("bearer ")) {
			token = request.getHeader(AUTHORIZATION_HEADER).split(" ")[1];
		} else {
			return null;
		}
		Optional<JwtToken> jwtBody = jwtTokenService.decode(token);
		if (jwtBody.isEmpty()) {
			throw new BadCredentialsException("Broken JWT or an unknown sign key");
		} else {
			return new JwtAuthentication(
				Collections.emptyList(),
				jwtBody.get()
			);
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			Authentication authentication = check(request, response);
			if (authentication != null) {
				Authentication auth = authenticationManager.authenticate(authentication);
				SecurityContextHolder.createEmptyContext();
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (AuthenticationException e) {
			SecurityContextHolder.clearContext();
			logger.debug("AuthenticationException on jwt", e);
			response.setStatus(403);
			return;
		}
		filterChain.doFilter(request, response);
	}
}