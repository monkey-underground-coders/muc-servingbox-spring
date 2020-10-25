package com.a6raywa1cher.mucservingboxspring.rest.res;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.security.jwt.JwtRefreshPair;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemporaryUserCreateResponse {
	@JsonView(Views.Public.class)
	private User user;

	@JsonView(Views.Public.class)
	private JwtRefreshPair pair;
}
