package com.a6raywa1cher.mucservingboxspring.security.jwt.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.jpa.RefreshToken;
import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtRefreshPair;
import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtToken;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.JwtRefreshPairService;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.JwtTokenService;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.RefreshTokenService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class JwtRefreshPairServiceImpl implements JwtRefreshPairService {
	private final RefreshTokenService refreshTokenService;

	private final JwtTokenService jwtTokenService;

	public JwtRefreshPairServiceImpl(RefreshTokenService refreshTokenService, JwtTokenService jwtTokenService) {
		this.refreshTokenService = refreshTokenService;
		this.jwtTokenService = jwtTokenService;
	}

	@Override
	public JwtRefreshPair issue(User user) {
		RefreshToken refreshToken = refreshTokenService.issue(user);
		JwtToken accessToken = jwtTokenService.issue(user.getId(), refreshToken.getId());
		return new JwtRefreshPair(
			refreshToken.getToken(),
			OffsetDateTime.of(refreshToken.getExpiringAt(), OffsetDateTime.now().getOffset()),
			accessToken.getToken(),
			OffsetDateTime.of(accessToken.getExpiringAt(), OffsetDateTime.now().getOffset())
		);
	}
}
