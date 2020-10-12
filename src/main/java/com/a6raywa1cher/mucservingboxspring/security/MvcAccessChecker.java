package com.a6raywa1cher.mucservingboxspring.security;

import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MvcAccessChecker {
	public boolean checkChildrenPathAccess(Long id, ActionType actionType, Authentication authentication) {
		log.error("meow");
		return true;
	}
}
