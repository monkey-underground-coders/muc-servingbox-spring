package com.a6raywa1cher.mucservingboxspring.utils;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.authentication.CustomAuthentication;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationResolverImpl implements AuthenticationResolver {
	private final UserService userService;

	public AuthenticationResolverImpl(UserService userService) {
		this.userService = userService;
	}

	@Override
	public User getUser() throws AuthenticationException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return getUser(authentication);
	}

	@Override
	public User getUser(Authentication authentication) throws AuthenticationException {
		if (authentication == null) {
			throw new BadCredentialsException("No credentials presented");
		}
		if (authentication instanceof CustomAuthentication) {
			CustomAuthentication customAuthentication = (CustomAuthentication) authentication;
			return userService.getById(customAuthentication.getPrincipal()).orElseThrow();
		} else if (authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
			return userService.getById((Long) token.getPrincipal()).orElseThrow();
		}
		throw new AuthenticationResolveException("Unknown Authentication " + authentication.getClass().getCanonicalName());
	}
}
