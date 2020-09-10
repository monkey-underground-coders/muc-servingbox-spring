package com.a6raywa1cher.mucservingboxspring.security.jwt.service;

public interface BlockedRefreshTokensService {
	void invalidate(Long id);

	boolean isValid(Long id);
}
