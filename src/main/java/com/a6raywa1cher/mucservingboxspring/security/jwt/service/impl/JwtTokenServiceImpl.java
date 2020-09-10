package com.a6raywa1cher.mucservingboxspring.security.jwt.service.impl;

import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtToken;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.JwtTokenService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {
	private final static String ISSUER_NAME = "onlineshop-spring";
	private final static String REFRESH_TOKEN_ID_CLAIM = "rti";
	@Value("${jwt.secret}")
	private String secret;
	private Algorithm algorithm;
	private JWTVerifier jwtVerifier;
	@Value("${jwt.access-duration}")
	private Duration duration;

	@PostConstruct
	public void init() {
		algorithm = Algorithm.HMAC512(secret);
		jwtVerifier = JWT.require(algorithm)
			.withIssuer(ISSUER_NAME)
			.build();
	}

	@Override
	public JwtToken issue(Long userId, Long refreshId) {
		ZonedDateTime expiringAt = nowPlusDuration();
		String token = JWT.create()
			.withIssuer(ISSUER_NAME)
			.withSubject(Long.toString(userId))
			.withExpiresAt(Date.from(expiringAt.toInstant()))
			.withClaim(REFRESH_TOKEN_ID_CLAIM, refreshId)
			.sign(algorithm);
		return JwtToken.builder()
			.token(token)
			.uid(userId)
			.expiringAt(expiringAt.toLocalDateTime())
			.build();
	}

	private ZonedDateTime nowPlusDuration() {
		return ZonedDateTime.now().plus(duration);
	}

	@Override
	public Optional<JwtToken> decode(String token) {
		try {
			DecodedJWT decodedJWT = jwtVerifier.verify(token);
			JwtToken jwtToken = JwtToken.builder()
				.token(token)
				.uid(Long.parseLong(decodedJWT.getSubject()))
				.expiringAt(decodedJWT.getExpiresAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
				.refreshId(decodedJWT.getClaim(REFRESH_TOKEN_ID_CLAIM).asLong())
				.build();
			return Optional.of(jwtToken);
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
