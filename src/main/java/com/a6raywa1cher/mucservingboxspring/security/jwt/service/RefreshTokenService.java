package com.a6raywa1cher.mucservingboxspring.security.jwt.service;


import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.jpa.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
	RefreshToken issue(User user);

	Optional<RefreshToken> getByToken(String token);

	void invalidate(RefreshToken refreshToken);

	void invalidateAll(User user);
}
