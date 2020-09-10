package com.a6raywa1cher.mucservingboxspring.security.authentication;

import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthentication extends AbstractAuthenticationToken {
	private JwtToken jwtToken;

	public JwtAuthentication(Collection<? extends GrantedAuthority> authorities, JwtToken jwtToken) {
		super(authorities);
		this.jwtToken = jwtToken;
	}

	@Override
	public JwtToken getCredentials() {
		return jwtToken;
	}

	@Override
	public Long getPrincipal() {
		return jwtToken.getUid();
	}
}
