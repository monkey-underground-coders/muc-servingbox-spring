package com.a6raywa1cher.mucservingboxspring.security.providers;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.SecurityConstants;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {
	private final PasswordEncoder passwordEncoder;
	private final UserService userService;

	public UsernamePasswordAuthenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof UsernamePasswordAuthenticationToken) ||
			!(authentication.getPrincipal() instanceof String) ||
			!(authentication.getCredentials() instanceof String)) {
			return null;
		}
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
		String email = (String) token.getPrincipal();
		Optional<User> user = userService.getByUsername(email);
		String inputPassword = (String) authentication.getCredentials();
		if (user.isEmpty() || !passwordEncoder.matches(inputPassword, user.get().getPassword())) {
			throw new BadCredentialsException("User not exists or incorrect password");
		}
		if ("".equals(user.get().getPassword())) {
			throw new DisabledException("User didn't set up password");
		}
		return new UsernamePasswordAuthenticationToken(
			user.get().getId(), token, Collections.singletonList(new SimpleGrantedAuthority(SecurityConstants.CONVERTIBLE)));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
