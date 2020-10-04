package com.a6raywa1cher.mucservingboxspring.security.providers;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.SecurityConstants;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.format.DateTimeFormatter;
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
		Optional<User> byUsername = userService.getByUsername(email);
		String inputPassword = (String) authentication.getCredentials();
		if (byUsername.isEmpty() || !passwordEncoder.matches(inputPassword, byUsername.get().getPassword())) {
			throw new BadCredentialsException("User not exists or incorrect password");
		}
		User user = byUsername.get();
		if (!user.isEnabled()) {
			throw new AccountExpiredException(String.format("Account %d expired at %s", user.getId(), user.getExpiringAt().format(DateTimeFormatter.ISO_INSTANT)));
		}
		if ("".equals(user.getPassword())) {
			throw new DisabledException("User didn't set up password");
		}
		return new UsernamePasswordAuthenticationToken(
			user.getId(), token, Collections.singletonList(new SimpleGrantedAuthority(SecurityConstants.CONVERTIBLE)));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
