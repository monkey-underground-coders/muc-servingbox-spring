package com.a6raywa1cher.mucservingboxspring.utils;


import com.a6raywa1cher.mucservingboxspring.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface AuthenticationResolver {
	User getUser() throws AuthenticationException;

	User getUser(Authentication authentication) throws AuthenticationException;
}
