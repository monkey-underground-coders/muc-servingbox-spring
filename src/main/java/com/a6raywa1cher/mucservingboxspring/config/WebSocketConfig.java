package com.a6raywa1cher.mucservingboxspring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private TaskScheduler messageBrokerTaskScheduler;
	private AppConfigProperties properties;

	@Autowired
	public WebSocketConfig(AppConfigProperties properties) {
		this.properties = properties;
	}

	@Autowired
	public void setMessageBrokerTaskScheduler(TaskScheduler taskScheduler) {
		this.messageBrokerTaskScheduler = taskScheduler;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOrigins(properties.getCorsAllowedOrigins())
			.addInterceptors(new HttpSessionHandshakeInterceptor());
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");
		registry.enableSimpleBroker("/topic")
			.setHeartbeatValue(new long[]{10000, 20000})
			.setTaskScheduler(this.messageBrokerTaskScheduler);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		registry.setMessageSizeLimit(128 * 1024);
	}
}
