package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.rest.exc.UnacceptableRequestTowardsTemporaryUserException;
import com.a6raywa1cher.mucservingboxspring.rest.req.ChangeNameOfUserRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.ChangePasswordRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreateUserRequest;
import com.a6raywa1cher.mucservingboxspring.rest.res.TemporaryUserCreateResponse;
import com.a6raywa1cher.mucservingboxspring.security.jwt.service.JwtRefreshPairService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.utils.LocalHtmlUtils;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Transactional(rollbackOn = Exception.class)
public class UserController {
	private final UserService userService;
	private final FSEntityService fsEntityService;
	private final JwtRefreshPairService jwtRefreshPairService;

	public UserController(UserService userService, FSEntityService fsEntityService,
						  JwtRefreshPairService jwtRefreshPairService) {
		this.userService = userService;
		this.fsEntityService = fsEntityService;
		this.jwtRefreshPairService = jwtRefreshPairService;
	}

	@PostMapping("/temp")
	@JsonView(Views.Internal.class)
	public ResponseEntity<TemporaryUserCreateResponse> createTemporaryUser(HttpServletRequest request) {
		User user = userService.create(request.getRemoteAddr());
		return ResponseEntity.ok(new TemporaryUserCreateResponse(user, jwtRefreshPairService.issue(user)));
	}

	@PostMapping("/create")
	@Secured({"ROLE_ADMIN"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Internal.class)
	public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest request, HttpServletRequest servletRequest) {
		User user = userService.create(request.getUserRole(), request.getUsername(),
			LocalHtmlUtils.htmlEscape(request.getName(), 255), request.getPassword(), servletRequest.getRemoteAddr());
		User out = userService.editRootFolder(user, fsEntityService.createNewHome(user));
		return ResponseEntity.ok(out);
	}

	@GetMapping("/{uid:[0-9]+}")
	@JsonView(Views.Public.class)
	public ResponseEntity<User> getById(@PathVariable long uid) {
		return userService.getById(uid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{uid:[0-9]+}/internal")
	@Secured({"ROLE_ADMIN"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Internal.class)
	public ResponseEntity<User> getByIdInternal(@PathVariable long uid) {
		return userService.getById(uid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/{uid:[0-9]+}/edit_name")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Internal.class)
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
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Internal.class)
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
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Internal.class)
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
