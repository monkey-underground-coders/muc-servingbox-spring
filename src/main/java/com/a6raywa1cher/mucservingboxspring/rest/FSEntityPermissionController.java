package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.ActionType;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.rest.exc.ApplicationDefinedPermissionException;
import com.a6raywa1cher.mucservingboxspring.rest.exc.ExpectingAnySubjectException;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreatePermissionRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.EditPermissionRequest;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/permission")
@Transactional(rollbackOn = Exception.class)
public class FSEntityPermissionController {
	private final FSEntityPermissionService permissionService;
	private final FSEntityService entityService;
	private final UserService userService;

	@Autowired
	public FSEntityPermissionController(FSEntityPermissionService permissionService, FSEntityService entityService,
										UserService userService) {
		this.permissionService = permissionService;
		this.entityService = entityService;
		this.userService = userService;
	}

	@GetMapping("/{pid:[0-9]+}")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<FSEntityPermission> getById(@PathVariable long pid) {
		return permissionService.getById(pid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/entity/{fid:[0-9]+}")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<List<FSEntityPermission>> getByEntity(@PathVariable long fid) {
		Optional<FSEntity> optional = entityService.getById(fid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(permissionService.getByFSEntity(optional.get()));
	}

	@PostMapping("/create")
	@PreAuthorize("@mvcAccessChecker.checkEntityAccessById(#request.getEntityId(), 'perm')")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<FSEntityPermission> createPermission(@RequestBody @Valid CreatePermissionRequest request) {
		Optional<FSEntity> optionalFSEntity = entityService.getById(request.getEntityId());
		if (optionalFSEntity.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		List<User> userList = userService.getById(request.getUserIds()).collect(Collectors.toList());
		if (userList.size() != request.getUserIds().size()) {
			return ResponseEntity.notFound().build();
		}
		if (userList.size() == 0 && request.getUserRoles().size() == 0) {
			throw new ExpectingAnySubjectException();
		}
		return ResponseEntity.ok(
			permissionService.create(optionalFSEntity.get(), userList, request.getUserRoles(),
				false, request.getActionTypes())
		);
	}

	@PutMapping("/{pid:[0-9]+}")
	@PreAuthorize("@mvcAccessChecker.checkPermissionAccess(#pid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<FSEntityPermission> editPermission(@RequestBody @Valid EditPermissionRequest request,
															 @PathVariable long pid) {
		Optional<FSEntityPermission> optional = permissionService.getById(pid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntityPermission permission = optional.get();
		if (permission.getApplicationDefined()) {
			throw new ApplicationDefinedPermissionException();
		}
		Optional<FSEntity> optionalFSEntity = entityService.getById(request.getEntityId());
		if (optionalFSEntity.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		List<User> userList = userService.getById(request.getUserIds()).collect(Collectors.toList());
		if (userList.size() != request.getUserIds().size()) {
			return ResponseEntity.notFound().build();
		}
		if (userList.size() == 0 && request.getUserRoles().size() == 0) {
			throw new ExpectingAnySubjectException();
		}
		return ResponseEntity.ok(
			permissionService.edit(permission, optionalFSEntity.get(), userList, request.getUserRoles(),
				false, request.getActionTypes())
		);
	}

	@GetMapping("/entity/{fid:[0-9]+}/children/")
	@PreAuthorize("@mvcAccessChecker.checkLowerAccessById(#fid, 'read')")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<List<FSEntity>> getChildrenFSEntities(@PathVariable long fid, @Parameter(hidden = true) User user) {
		Optional<FSEntity> optional = entityService.getById(fid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(
			permissionService.getAllChildrenWithAccess(optional.get(), user, ActionType.READ)
		);
	}

	@DeleteMapping("/{pid:[0-9]+}")
	@PreAuthorize("@mvcAccessChecker.checkPermissionAccess(#pid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<Void> deletePermission(@PathVariable long pid) {
		Optional<FSEntityPermission> optional = permissionService.getById(pid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntityPermission permission = optional.get();
		if (permission.getApplicationDefined()) {
			throw new ApplicationDefinedPermissionException();
		}
		permissionService.delete(permission);
		return ResponseEntity.ok().build();
	}
}
