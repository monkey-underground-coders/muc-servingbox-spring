package com.a6raywa1cher.mucservingboxspring.security.jwt.service.impl;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.jpa.RefreshToken;
import com.a6raywa1cher.mucservingboxspring.security.jpa.repo.RefreshTokenRepository;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.BlockedRefreshTokensService;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {
	private final RefreshTokenRepository repository;
	private final BlockedRefreshTokensService service;
	private final long maxTokensPerUser;
	private final Duration refreshTokenDuration;

	public RefreshTokenServiceImpl(RefreshTokenRepository repository, BlockedRefreshTokensService service,
								   @Value("${jwt.max-refresh-tokens-per-user}") Long maxTokensPerUser,
								   @Value("${jwt.refresh-duration}") Duration refreshTokenDuration) {
		this.repository = repository;
		this.service = service;
		this.maxTokensPerUser = maxTokensPerUser;
		this.refreshTokenDuration = refreshTokenDuration;
	}

	@Override
	public RefreshToken issue(User user) {
		List<RefreshToken> tokenList = repository.findAllByUser(user);
		if (tokenList.size() > maxTokensPerUser) {
			repository.deleteAll(tokenList.stream()
				.sorted(Comparator.comparing(RefreshToken::getExpiringAt))
				.limit(tokenList.size() - 5)
				.peek(rt -> service.invalidate(rt.getId()))
				.collect(Collectors.toUnmodifiableList()));
		}
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setExpiringAt(LocalDateTime.now().plus(refreshTokenDuration));
		refreshToken.setUser(user);
		return repository.save(refreshToken);
	}

	@Override
	public Optional<RefreshToken> getByToken(String token) {
		return repository.findById(token);
	}

	@Override
	public void invalidate(RefreshToken refreshToken) {
		service.invalidate(refreshToken.getId());
		repository.delete(refreshToken);
	}

	@Override
	public void invalidateAll(User user) {
		repository.deleteAll(repository.findAllByUser(user).stream()
			.peek(rt -> service.invalidate(rt.getId()))
			.collect(Collectors.toList()));
	}
}
