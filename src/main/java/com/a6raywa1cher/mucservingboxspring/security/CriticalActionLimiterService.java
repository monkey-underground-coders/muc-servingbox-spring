package com.a6raywa1cher.mucservingboxspring.security;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CriticalActionLimiterService {
	private static final int MAX_ATTEMPT = 6;
	private final LoadingCache<String, Integer> attemptsCache;

	public CriticalActionLimiterService() {
		super();
		attemptsCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<>() {
				@Override
				public Integer load(String key) {
					return 0;
				}
			});
	}

	public void actionSucceed(String key) {
		attemptsCache.invalidate(key);
	}

	public void actionFailed(String key) {
		int attempts = attemptsCache.getUnchecked(key);
		attempts++;
		attemptsCache.put(key, attempts);
	}

	public boolean isBlocked(String key) {
		return attemptsCache.getUnchecked(key) >= MAX_ATTEMPT;
	}
}