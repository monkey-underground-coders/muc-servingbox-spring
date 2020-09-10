package com.a6raywa1cher.mucservingboxspring.security.jwt;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JwtToken {
	private String token;

	private LocalDateTime expiringAt;

	private long uid;

	private long refreshId;

	private String vendorSub;
}
