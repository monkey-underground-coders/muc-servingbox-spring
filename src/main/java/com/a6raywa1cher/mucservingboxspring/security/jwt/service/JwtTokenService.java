package com.a6raywa1cher.mucservingboxspring.security.jwt.service;


import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtToken;

import java.util.Optional;

public interface JwtTokenService {
	JwtToken issue(Long userId, Long refreshId);

	Optional<JwtToken> decode(String token);
}
