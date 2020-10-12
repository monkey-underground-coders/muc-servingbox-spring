package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.rest.exc.FileOperationOnFolderException;
import com.a6raywa1cher.mucservingboxspring.rest.exc.FileWithThisNameAlreadyExistsException;
import com.a6raywa1cher.mucservingboxspring.rest.exc.FolderOperationOnFileException;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreateFolderRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.MoveEntityRequest;
import com.a6raywa1cher.mucservingboxspring.service.FSEntityService;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.Optional;

@RestController
@RequestMapping("/fs")
@Transactional
public class FSEntityController {
	public static final String FS_NAME_REGEXP = "[^\\\\/:*?\"<>|]{3,255}";
	private final FSEntityService entityService;

	public FSEntityController(FSEntityService entityService) {
		this.entityService = entityService;
	}

	@PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<FSEntity> uploadFile(
		@RequestParam("file") MultipartFile multipartFile,
		@RequestParam("parent") long parentId,
		@RequestParam("name") @Pattern(regexp = FS_NAME_REGEXP) @Valid String fileName,
		@Parameter(hidden = true) User creator) {
		Optional<FSEntity> optionalParent = entityService.getById(parentId);
		if (optionalParent.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntity parent = optionalParent.get();
		if (!parent.isFolder()) {
			throw new FolderOperationOnFileException();
		}
		if (isNonUniqueInThisParent(fileName, parent)) {
			throw new FileWithThisNameAlreadyExistsException();
		}
		return ResponseEntity.ok(entityService.createNewFile(parent, fileName, multipartFile, false, creator));
	}

	@PostMapping("/folder")
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<FSEntity> createFolder(@RequestBody @Valid CreateFolderRequest request, @Parameter(hidden = true) User creator) {
		Optional<FSEntity> optionalParent = entityService.getById(request.getParentId());
		if (optionalParent.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntity parent = optionalParent.get();
		if (!parent.isFolder()) {
			throw new FolderOperationOnFileException();
		}
		if (isNonUniqueInThisParent(request.getFolderName(), parent)) {
			throw new FileWithThisNameAlreadyExistsException();
		}
		return ResponseEntity.ok(entityService.createNewFolder(parent, request.getFolderName(), false, creator));
	}

	@PutMapping(path = "/{fid:[0-9]+}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Void> updateContent(@PathVariable long fid, @RequestParam("file") MultipartFile multipartFile) {
		Optional<FSEntity> optionalFSEntity = entityService.getById(fid);
		if (optionalFSEntity.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntity file = optionalFSEntity.get();
		if (file.isFolder()) {
			throw new FileOperationOnFolderException();
		}
		entityService.modifyFile(file, multipartFile);
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/{fid:[0-9]+}/content", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Resource> getContent(@PathVariable long fid) {
		Optional<FSEntity> optionalFSEntity = entityService.getById(fid);
		if (optionalFSEntity.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntity file = optionalFSEntity.get();
		if (file.isFolder()) {
			throw new FileOperationOnFolderException();
		}
		return ResponseEntity.ok()
			.header("Content-Disposition", "attachment; filename=\"" + file.getName() + '"')
			.body(entityService.getFileContent(file));
	}

	@PostMapping("/move")
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<FSEntity> moveEntity(@RequestBody @Valid MoveEntityRequest request, @Parameter(hidden = true) User user) {
		Optional<FSEntity> optionalObject = entityService.getById(request.getObjectId());
		Optional<FSEntity> optionalTargetParent = entityService.getById(request.getTargetParentId());
		if (optionalObject.isEmpty() || optionalTargetParent.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntity object = optionalObject.get();
		FSEntity targetParent = optionalTargetParent.get();
		if (!targetParent.isFolder()) {
			throw new FolderOperationOnFileException();
		}
		if (isNonUniqueInThisParent(request.getEntityName(), targetParent)) {
			throw new FileWithThisNameAlreadyExistsException();
		}
		return ResponseEntity.ok(entityService.moveEntity(object, targetParent, request.getEntityName(), false, user));
	}

	@PostMapping("/copy")
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<FSEntity> copyEntity(@RequestBody @Valid MoveEntityRequest request, @Parameter(hidden = true) User user) {
		Optional<FSEntity> optionalObject = entityService.getById(request.getObjectId());
		Optional<FSEntity> optionalTargetParent = entityService.getById(request.getTargetParentId());
		if (optionalObject.isEmpty() || optionalTargetParent.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		FSEntity object = optionalObject.get();
		FSEntity targetParent = optionalTargetParent.get();
		if (!targetParent.isFolder()) {
			throw new FolderOperationOnFileException();
		}
		if (isNonUniqueInThisParent(request.getEntityName(), targetParent)) {
			throw new FileWithThisNameAlreadyExistsException();
		}
		return ResponseEntity.ok(entityService.copyEntity(object, targetParent, request.getEntityName(), false, user));
	}

	@GetMapping("/{fid:[0-9]+}")
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<FSEntity> getById(@PathVariable long fid) {
		return entityService.getById(fid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{fid:[0-9]+}")
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Void> delete(@PathVariable long fid) {
		Optional<FSEntity> optionalFSEntity = entityService.getById(fid);
		if (optionalFSEntity.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		entityService.deleteEntity(optionalFSEntity.get());
		return ResponseEntity.ok().build();
	}


	private boolean isNonUniqueInThisParent(String fileName, FSEntity parent) {
		return entityService.getByPath(parent.getPath() + fileName).isPresent() ||
			entityService.getByPath(parent.getPath() + fileName + '/').isPresent();
	}
}
