package com.a6raywa1cher.mucservingboxspring.security.providers;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.security.authentication.CustomAuthentication;
import com.a6raywa1cher.mucservingboxspring.security.authentication.JwtAuthentication;
import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtToken;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.BlockedRefreshTokensService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
	private final UserService userService;
	private final BlockedRefreshTokensService service;

	public JwtAuthenticationProvider(UserService userService, BlockedRefreshTokensService service) {
		this.userService = userService;
		this.service = service;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!supports(authentication.getClass())) {
			return null;
		}
		JwtAuthentication customAuthentication = (JwtAuthentication) authentication;
		JwtToken jwtToken = customAuthentication.getCredentials();
		if (jwtToken == null) {
			customAuthentication.setAuthenticated(false);
			throw new BadCredentialsException("JwtToken not provided");
		}
		if (!service.isValid(jwtToken.getRefreshId())) {
			throw new CredentialsExpiredException("Refresh-token was revoked");
		}
		Long userId = jwtToken.getUid();
		Optional<User> byId = userService.getById(userId);
		if (byId.isEmpty()) {
			customAuthentication.setAuthenticated(false);
			throw new UsernameNotFoundException(String.format("User %d doesn't exists", userId));
		}
		User user = byId.get();
		if (!user.isEnabled()) {
			throw new AccountExpiredException(String.format("Account %d expired at %s", user.getId(), user.getExpiringAt().format(DateTimeFormatter.ISO_INSTANT)));
		}
		UserRole userRole = user.getUserRole();
		Set<GrantedAuthority> authoritySet = userRole.access.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
			.collect(Collectors.toUnmodifiableSet());
		return new CustomAuthentication(authoritySet, jwtToken);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return JwtAuthentication.class.isAssignableFrom(authentication);
	}
}
