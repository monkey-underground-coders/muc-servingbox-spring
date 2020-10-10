package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.rest.exc.UnacceptableRequestTowardsTemporaryUserException;
import com.a6raywa1cher.mucservingboxspring.rest.req.ChangeNameOfUserRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.ChangePasswordRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreateUserRequest;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
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
	private final FSEntityService fsEntityService;

	public UserController(UserService userService, FSEntityService fsEntityService) {
		this.userService = userService;
		this.fsEntityService = fsEntityService;
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
		User user = userService.create(request.getUserRole(), request.getUsername(),
			LocalHtmlUtils.htmlEscape(request.getName(), 255), request.getPassword(), servletRequest.getRemoteAddr());
		User out = userService.editRootFolder(user, fsEntityService.createNewHome(user));
		return ResponseEntity.ok(out);
	}

	@PutMapping("/{uid:[0-9]+}/edit_name")
	public ResponseEntity<User> editName(@RequestBody @Valid ChangeNameOfUserRequest request, @PathVariable long uid) {
		Optional<User> optional = userService.getById(uid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		User user = optional.get();
		return ResponseEntity.ok(userService.editUser(user, user.getUserRole(), user.getUsername(),
			LocalHtmlUtils.htmlEscape(request.getName(), 255)));
	}

	@PutMapping("/{uid:[0-9]+}/edit_password")
	public ResponseEntity<User> editPassword(@RequestBody @Valid ChangePasswordRequest request, @PathVariable long uid) {
		Optional<User> optional = userService.getById(uid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		User user = optional.get();
		if (user.getUserRole() == UserRole.TEMPORARY_USER) {
			throw new UnacceptableRequestTowardsTemporaryUserException();
		}
		return ResponseEntity.ok(userService.editPassword(user, request.getPassword()));
	}

	@DeleteMapping("/{uid:[0-9]+}")
	public ResponseEntity<Void> delete(@PathVariable long uid) {
		Optional<User> optional = userService.getById(uid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		User user = optional.get();
		if (user.getRootFolder() != null) {
			fsEntityService.deleteEntity(user.getRootFolder());
		}
		userService.deleteUser(user);
		return ResponseEntity.ok().build();
	}
}