package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.predicate.impl.LessonSchemaPredicate;
import com.a6raywa1cher.mucservingboxspring.rest.exc.TransferOfNonOnFlyException;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreateLessonSchemaRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.EditLessonSchemaRequest;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.utils.LocalHtmlUtils;
import com.a6raywa1cher.mucservingboxspring.utils.RestUtils;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/schema")
@Transactional(rollbackOn = Exception.class)
public class LessonSchemaController {
	private final LessonSchemaService schemaService;

	public LessonSchemaController(LessonSchemaService schemaService) {
		this.schemaService = schemaService;
	}

	@PostMapping("/create")
	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Detailed.class)
	public ResponseEntity<LessonSchema> createLessonSchema(@RequestBody @Valid CreateLessonSchemaRequest request,
														   @Parameter(hidden = true) User user) {
		return ResponseEntity.ok(schemaService.create(
			LocalHtmlUtils.htmlEscape(request.getTitle(), 255),
			LocalHtmlUtils.htmlEscape(request.getDescription()),
			user
		));
	}

	@GetMapping("/{lid:[0-9]+}")
	@PreAuthorize("@mvcAccessChecker.checkSchemaReadAccess(#lid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Detailed.class)
	public ResponseEntity<LessonSchema> getById(@PathVariable long lid) {
		return schemaService.getById(lid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/self")
	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Detailed.class)
	@PageableAsQueryParam
	public ResponseEntity<Page<LessonSchema>> searchSelf(@RequestParam(required = false) String filter,
														 @Parameter(hidden = true) User user,
														 @Parameter(hidden = true) Pageable pageable) {
		BooleanExpression booleanExpression = RestUtils.decodeFilter(filter, LessonSchemaPredicate.class);
		return ResponseEntity.ok(schemaService.getPage(booleanExpression, user, pageable));
	}

	@GetMapping("/page")
	@Secured({"ROLE_ADMIN"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Detailed.class)
	@PageableAsQueryParam
	public ResponseEntity<Page<LessonSchema>> search(@RequestParam(required = false) String filter,
													 @Parameter(hidden = true) Pageable pageable) {
		BooleanExpression booleanExpression = RestUtils.decodeFilter(filter, LessonSchemaPredicate.class);
		return ResponseEntity.ok(schemaService.getPage(booleanExpression, pageable));
	}

	@PutMapping("/{lid:[0-9]+}")
	@PreAuthorize("@mvcAccessChecker.checkSchemaWriteAccess(#lid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Detailed.class)
	public ResponseEntity<LessonSchema> editLessonSchema(@RequestBody @Valid EditLessonSchemaRequest request, @PathVariable long lid) {
		Optional<LessonSchema> byId = schemaService.getById(lid);
		if (byId.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(schemaService.editSchema(byId.get(),
			LocalHtmlUtils.htmlEscape(request.getTitle(), 255),
			LocalHtmlUtils.htmlEscape(request.getDescription())
		));
	}

	@PostMapping("/{lid:[0-9]+}/transfer")
	@PreAuthorize("@mvcAccessChecker.checkSchemaWriteAccess(#lid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Detailed.class)
	public ResponseEntity<LessonSchema> transfer(@PathVariable long lid) {
		Optional<LessonSchema> byId = schemaService.getById(lid);
		if (byId.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LessonSchema lessonSchema = byId.get();
		if (!lessonSchema.isOnTheFly()) {
			throw new TransferOfNonOnFlyException();
		}
		return ResponseEntity.ok(schemaService.transferToNotOnFly(lessonSchema));
	}

	@DeleteMapping("/{lid:[0-9]+}")
	@PreAuthorize("@mvcAccessChecker.checkSchemaWriteAccess(#lid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Void> delete(@PathVariable long lid) {
		Optional<LessonSchema> byId = schemaService.getById(lid);
		if (byId.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		schemaService.deleteSchema(byId.get());
		return ResponseEntity.ok().build();
	}
}
