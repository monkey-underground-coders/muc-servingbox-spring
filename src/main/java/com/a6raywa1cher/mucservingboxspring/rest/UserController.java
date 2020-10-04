package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreateUserRequest;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/temp")
	@JsonView(Views.Internal.class)
	public ResponseEntity<User> createTemporaryUser(HttpServletRequest request) {
		return ResponseEntity.ok(userService.create(request.getRemoteAddr()));
	}

	@PostMapping("/create")
//	@PreAuthorize("#mvcAccessChecker.haveAccessToCreateUser(#request.getUserRole(), #authentication)")
	@JsonView(Views.Internal.class)
	public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest request, HttpServletRequest servletRequest) {
		return ResponseEntity.ok(userService.create(request.getUserRole(), request.getUsername(), request.getName(), request.getPassword(), servletRequest.getRemoteAddr()));
	}
}
