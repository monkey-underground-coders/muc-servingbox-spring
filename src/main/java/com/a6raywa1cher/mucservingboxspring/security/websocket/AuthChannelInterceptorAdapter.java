package com.a6raywa1cher.mucservingboxspring.security.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

// https://stackoverflow.com/questions/45405332/websocket-authentication-and-authorization-in-spring
@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {
	private final AuthenticationManager authenticationManager;

	@Autowired
	public AuthChannelInterceptorAdapter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
		final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (StompCommand.CONNECT == accessor.getCommand()) {
			final String username = accessor.getLogin();
			if (username == null) {
				return message;
			}
			final String password = accessor.getPasscode();

			final UsernamePasswordAuthenticationToken raw = new UsernamePasswordAuthenticationToken(username, password);
			final Authentication user = authenticationManager.authenticate(raw);

			accessor.setUser(user);
		}
		return message;
	}
}