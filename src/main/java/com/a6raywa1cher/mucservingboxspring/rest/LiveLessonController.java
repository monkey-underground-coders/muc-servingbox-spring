package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LiveLesson;
import com.a6raywa1cher.mucservingboxspring.rest.exc.PastDateModificationException;
import com.a6raywa1cher.mucservingboxspring.rest.exc.UserAlreadyConnectedException;
import com.a6raywa1cher.mucservingboxspring.rest.req.EditLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.ScheduleLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.StartLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.StartOnTheFlyLiveLessonRequest;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.LiveLessonService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.utils.LocalHtmlUtils;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/live")
public class LiveLessonController {
	private final LessonSchemaService schemaService;
	private final LiveLessonService liveLessonService;
	private final UserService userService;

	public LiveLessonController(LessonSchemaService schemaService, LiveLessonService liveLessonService,
								UserService userService) {
		this.schemaService = schemaService;
		this.liveLessonService = liveLessonService;
		this.userService = userService;
	}

	@PostMapping("/schedule")
	public ResponseEntity<LiveLesson> schedule(@RequestBody @Valid ScheduleLiveLessonRequest request, @Parameter(hidden = true) User user) {
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
	public ResponseEntity<LiveLesson> startOnTheFly(@RequestBody @Valid StartOnTheFlyLiveLessonRequest request, @Parameter(hidden = true) User user) {
		return ResponseEntity.ok(liveLessonService.startOnTheFly(
			LocalHtmlUtils.htmlEscape(request.getName(), 255),
			request.getEnd(),
			user
		));
	}

	@GetMapping("/{llid:[0-9]+}")
	public ResponseEntity<LiveLesson> getById(@PathVariable long llid) {
		return liveLessonService.getById(llid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/schema/{lid:[0-9]+}")
	public ResponseEntity<Page<LiveLesson>> getBySchema(@PathVariable long lid, Pageable pageable) {
		return schemaService.getById(lid)
			.map(s -> liveLessonService.getPageBySchema(s, pageable))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{uid:[0-9]+}")
	public ResponseEntity<Page<LiveLesson>> getByCreator(@PathVariable long uid, @RequestParam List<String> words,
														 Pageable pageable) {
		return userService.getById(uid).map(u -> liveLessonService.getPageByCreator(words, pageable, u))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/page")
	public ResponseEntity<Page<LiveLesson>> getPage(@RequestParam List<String> words, Pageable pageable) {
		return ResponseEntity.ok(liveLessonService.getPage(words, pageable));
	}

	@PutMapping("/{llid:[0-9]+}")
	public ResponseEntity<LiveLesson> editLiveLesson(@RequestBody @Valid EditLiveLessonRequest request,
													 @PathVariable long llid) {
		Optional<LiveLesson> optionalLiveLesson = liveLessonService.getById(llid);
		if (optionalLiveLesson.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LiveLesson liveLesson = optionalLiveLesson.get();
		ZonedDateTime now = ZonedDateTime.now();
		if ((liveLesson.getStartAt().isBefore(now) && !liveLesson.getStartAt().equals(request.getStart())) ||
			(liveLesson.getEndAt().isBefore(now) && !liveLesson.getEndAt().equals(request.getEnd()))) {
			throw new PastDateModificationException();
		}
		return ResponseEntity.ok(liveLessonService.edit(liveLesson,
			LocalHtmlUtils.htmlEscape(request.getName(), 255),
			request.getStart(),
			request.getEnd()
		));
	}

	@PostMapping("/{llid:[0-9]+}/connect")
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
	public ResponseEntity<LiveLesson> stop(@PathVariable long llid) {
		return liveLessonService.getById(llid)
			.map(liveLessonService::stop)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{llid:[0-9]+}")
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
