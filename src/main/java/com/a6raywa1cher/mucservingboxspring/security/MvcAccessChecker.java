package com.a6raywa1cher.mucservingboxspring.security;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class MvcAccessChecker {
	public boolean haveAccessToCreateUser(UserRole requestedRole, Authentication authentication) {
		return false;
	}
}
