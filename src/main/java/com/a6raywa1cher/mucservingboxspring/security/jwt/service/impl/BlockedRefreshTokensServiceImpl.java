package com.a6raywa1cher.mucservingboxspring.security.jwt.service.impl;

import com.a6raywa1cher.mucservingboxspring.security.jwt.service.BlockedRefreshTokensService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class BlockedRefreshTokensServiceImpl implements BlockedRefreshTokensService {
	private final LoadingCache<Long, Long> cache;

	@Autowired
	public BlockedRefreshTokensServiceImpl(@Value("${jwt.access-duration}") Duration duration) {
		cache = CacheBuilder.newBuilder()
			.expireAfterWrite(duration)
			.build(new CacheLoader<>() {
				@Override
				public Long load(Long key) {
					return key;
				}
			});
	}

	@Override
	public void invalidate(Long uuid) {
		cache.put(uuid, uuid);
	}

	@Override
	public boolean isValid(Long uuid) {
		return cache.getIfPresent(uuid) == null;
	}
}
