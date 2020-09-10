package com.a6raywa1cher.mucservingboxspring.security;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Component
@Order(HIGHEST_PRECEDENCE)
public class CriticalActionLimiterFilter extends OncePerRequestFilter {
	private final CriticalActionLimiterService service;

	public CriticalActionLimiterFilter(CriticalActionLimiterService service) {
		this.service = service;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String ip = request.getRemoteAddr();
		if (service.isBlocked(ip)) {
			response.setStatus(429);
			response.setHeader("Retry-After", "60");
			SecurityContextHolder.clearContext();
		} else {
			filterChain.doFilter(request, response);
			if (response.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
				service.actionFailed(ip);
			} else {
				service.actionSucceed(ip);
			}
		}
	}
}
