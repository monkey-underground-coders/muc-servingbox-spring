package com.a6raywa1cher.mucservingboxspring.security.jwt.service;


import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtRefreshPair;

public interface JwtRefreshPairService {
	JwtRefreshPair issue(User user);
}
