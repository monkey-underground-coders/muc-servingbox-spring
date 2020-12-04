package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.model.predicate.impl.LiveLessonPredicate;
import com.a6raywa1cher.mucservingboxspring.rest.exc.PastDateModificationException;
import com.a6raywa1cher.mucservingboxspring.rest.exc.UserAlreadyConnectedException;
import com.a6raywa1cher.mucservingboxspring.rest.req.EditLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.ScheduleLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.StartLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.StartOnTheFlyLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.LiveLessonService;
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/live")
@Transactional(rollbackOn = Exception.class)
public class LiveLessonController {
	private final LessonSchemaService schemaService;
	private final LiveLessonService liveLessonService;

	public LiveLessonController(LessonSchemaService schemaService, LiveLessonService liveLessonService) {
		this.schemaService = schemaService;
		this.liveLessonService = liveLessonService;
	}

	@PostMapping("/schedule")
	@PreAuthorize("@mvcAccessChecker.checkSchemaWriteAccess(#request.getSchemaId())")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<LiveLesson> schedule(@RequestBody @Valid ScheduleLiveLessonRequest request,
											   @Parameter(hidden = true) User user) {
		Optional<LessonSchema> optional = schemaService.getById(request.getSchemaId());
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LessonSchema lessonSchema = optional.get();
		return ResponseEntity.ok(liveLessonService.schedule(
			LocalHtmlUtils.htmlEscape(request.getName(), 255),
			lessonSchema,
			request.getStart(),
			request.getEnd(),
			user
		));
	}

	@PostMapping("/start")
	@PreAuthorize("@mvcAccessChecker.checkSchemaWriteAccess(#request.getSchemaId())")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<LiveLesson> start(@RequestBody @Valid StartLiveLessonRequest request, @Parameter(hidden = true) User user) {
		Optional<LessonSchema> optional = schemaService.getById(request.getSchemaId());
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LessonSchema lessonSchema = optional.get();
		return ResponseEntity.ok(liveLessonService.start(
			LocalHtmlUtils.htmlEscape(request.getName(), 255),
			lessonSchema,
			request.getEnd(),
			user
		));
	}

	@PostMapping("/on_fly")
	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<LiveLesson> startOnTheFly(@RequestBody @Valid StartOnTheFlyLiveLessonRequest request, @Parameter(hidden = true) User user) {
		return ResponseEntity.ok(liveLessonService.startOnTheFly(
			LocalHtmlUtils.htmlEscape(request.getName(), 255),
			request.getEnd(),
			user
		));
	}

	@GetMapping("/{llid:[0-9]+}")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Detailed.class)
	public ResponseEntity<LiveLesson> getById(@PathVariable long llid) {
		return liveLessonService.getById(llid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/schema/{lid:[0-9]+}")
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<Page<LiveLesson>> getBySchema(@PathVariable long lid, Pageable pageable) {
		return schemaService.getById(lid)
			.map(s -> liveLessonService.getPageBySchema(s, pageable))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/self")
	@Secured({"ROLE_TEACHER", "ROLE_ADMIN"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	@PageableAsQueryParam
	public ResponseEntity<Page<LiveLesson>> getMyLiveLessons(@Parameter(hidden = true) Pageable pageable,
															 @Parameter(hidden = true) User user,
															 @RequestParam(required = false) String filter) {
		BooleanExpression booleanExpression = RestUtils.decodeFilter(filter, LiveLessonPredicate.class);
		return ResponseEntity.ok(liveLessonService.getPageByCreator(booleanExpression, pageable, user));
	}

	@GetMapping("/page")
	@Secured({"ROLE_ADMIN"})
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	@PageableAsQueryParam
	@Transactional
	public ResponseEntity<Page<LiveLesson>> getPage(@Parameter(hidden = true) Pageable pageable,
													@RequestParam(required = false) String filter) {
		BooleanExpression booleanExpression = RestUtils.decodeFilter(filter, LiveLessonPredicate.class);
		return ResponseEntity.ok(liveLessonService.getPage(booleanExpression, pageable));
	}

	@GetMapping("/active")
	@JsonView(Views.Public.class)
	@Transactional
	public ResponseEntity<List<LiveLesson>> getActiveList() {
		return ResponseEntity.ok(liveLessonService.getActiveList());
	}

	@PutMapping("/{llid:[0-9]+}")
	@PreAuthorize("@mvcAccessChecker.checkLiveLessonAccess(#llid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<LiveLesson> editLiveLesson(@RequestBody @Valid EditLiveLessonRequest request,
													 @PathVariable long llid) {
		Optional<LiveLesson> optionalLiveLesson = liveLessonService.getById(llid);
		if (optionalLiveLesson.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LiveLesson liveLesson = optionalLiveLesson.get();
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime startAt = liveLesson.getStartAt();
		ZonedDateTime endAt = liveLesson.getEndAt();
		ZonedDateTime requestStart = request.getStart();
		ZonedDateTime requestEnd = request.getEnd();
		if ((requestStart != null && startAt.isBefore(now) && !startAt.isEqual(requestStart)) ||
			(requestEnd != null && endAt.isBefore(now) && !endAt.isEqual(requestEnd))) {
			throw new PastDateModificationException();
		}
		return ResponseEntity.ok(liveLessonService.edit(liveLesson,
			LocalHtmlUtils.htmlEscape(request.getName(), 255),
			requestStart != null ? requestStart : startAt,
			requestEnd != null ? requestEnd : endAt
		));
	}

	@PostMapping("/{llid:[0-9]+}/connect")
	@JsonView(Views.Public.class)
	@Operation(security = @SecurityRequirement(name = "jwt"))
	public ResponseEntity<LiveLesson> connect(@PathVariable long llid, @Parameter(hidden = true) User user) {
		Optional<LiveLesson> optionalLiveLesson = liveLessonService.getById(llid);
		if (optionalLiveLesson.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LiveLesson liveLesson = optionalLiveLesson.get();
		if (liveLesson.getConnectedUsers().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
			throw new UserAlreadyConnectedException();
		}
		return ResponseEntity.ok(liveLessonService.connect(liveLesson, user));
	}

	@PostMapping("/{llid:[0-9]+}/stop")
	@PreAuthorize("@mvcAccessChecker.checkLiveLessonAccess(#llid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<LiveLesson> stop(@PathVariable long llid) {
		return liveLessonService.getById(llid)
			.map(liveLessonService::stop)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{llid:[0-9]+}")
	@PreAuthorize("@mvcAccessChecker.checkLiveLessonAccess(#llid)")
	@Operation(security = @SecurityRequirement(name = "jwt"))
	@JsonView(Views.Public.class)
	public ResponseEntity<Void> delete(@PathVariable long llid) {
		Optional<LiveLesson> optional = liveLessonService.getById(llid);
		if (optional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LiveLesson liveLesson = optional.get();
		liveLessonService.delete(liveLesson);
		return ResponseEntity.ok().build();
	}
}
