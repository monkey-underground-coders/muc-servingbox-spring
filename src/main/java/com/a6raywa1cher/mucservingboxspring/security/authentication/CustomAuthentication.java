package com.a6raywa1cher.mucservingboxspring.security.authentication;

import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthentication implements Authentication {
	private final Collection<? extends GrantedAuthority> authorities;
	private final Long userId;
	private final Object credentials;
	private boolean authenticated = true;

	public CustomAuthentication(Collection<? extends GrantedAuthority> authorities, JwtToken jwtToken) {
		this.authorities = authorities;
		this.credentials = jwtToken;
		this.userId = jwtToken.getUid();
	}

	public CustomAuthentication(Collection<? extends GrantedAuthority> authorities, Long userId, UsernamePasswordAuthenticationToken token) {
		this.authorities = authorities;
		this.credentials = token;
		this.userId = userId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Long getPrincipal() {
		return userId;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated) {
			throw new IllegalArgumentException();
		}
		authenticated = false;
	}

	@Override
	public String getName() {
		return Long.toString(userId);
	}
}
