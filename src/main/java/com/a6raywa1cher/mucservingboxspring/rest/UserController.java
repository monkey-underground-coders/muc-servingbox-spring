package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.rest.req.ChangeNameOfUserRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreateUserRequest;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.utils.LocalHtmlUtils;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

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
		return ResponseEntity.ok(userService.create(request.getUserRole(), request.getUsername(),
			LocalHtmlUtils.htmlEscape(request.getName()), request.getPassword(), servletRequest.getRemoteAddr()));
	}

	@PutMapping("/{uid:[0-9]+}/edit_name")
	public ResponseEntity<User> editName(@RequestBody @Valid ChangeNameOfUserRequest request, @PathVariable long uid) {
		Optional<User> optional = userService.getById(uid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		User user = optional.get();
		return ResponseEntity.ok(userService.editUser(user, user.getUserRole(), user.getUsername(),
			LocalHtmlUtils.htmlEscape(request.getName())));
	}


}
