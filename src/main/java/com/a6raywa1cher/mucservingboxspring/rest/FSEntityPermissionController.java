package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreatePermissionRequest;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityPermissionService;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/permission")
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
	public ResponseEntity<FSEntityPermission> getById(@PathVariable long pid) {
		return permissionService.getById(pid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/create")
	public ResponseEntity<FSEntityPermission> createPermission(@RequestBody @Valid CreatePermissionRequest request) {
		List<FSEntity> fsEntityList = entityService.getById(request.getEntityIds()).collect(Collectors.toList());
		if (fsEntityList.size() != request.getEntityIds().size()) {
			return ResponseEntity.notFound().build();
		}
		List<User> userList = userService.getById(request.getUserIds()).collect(Collectors.toList());
		if (userList.size() != request.getUserIds().size()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(
			permissionService.create(fsEntityList, userList, request.getUserRoles(),
				false, request.getActionTypes())
		);
	}

	@DeleteMapping("/{pid:[0-9]+}")
	public ResponseEntity<Void> deletePermission(@PathVariable long pid) {
		Optional<FSEntityPermission> optional = permissionService.getById(pid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		permissionService.delete(optional.get());
		return ResponseEntity.ok().build();
	}
}
