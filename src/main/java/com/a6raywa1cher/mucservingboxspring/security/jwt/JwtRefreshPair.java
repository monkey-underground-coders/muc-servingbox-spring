package com.a6raywa1cher.mucservingboxspring.security.jwt;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class JwtRefreshPair {
	private String refreshToken;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime refreshTokenExpiringAt;

	private String accessToken;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime accessTokenExpiringAt;
}
