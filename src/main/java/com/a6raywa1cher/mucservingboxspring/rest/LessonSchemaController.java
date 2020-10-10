package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.rest.req.CreateLessonSchemaRequest;
import com.a6raywa1cher.mucservingboxspring.rest.req.EditLessonSchemaRequest;
import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.UserService;
import com.a6raywa1cher.mucservingboxspring.utils.LocalHtmlUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schema")
public class LessonSchemaController {
	private final LessonSchemaService schemaService;
	private final UserService userService;

	public LessonSchemaController(LessonSchemaService schemaService, UserService userService) {
		this.schemaService = schemaService;
		this.userService = userService;
	}

	@PostMapping("/create")
	public ResponseEntity<LessonSchema> createLessonSchema(@RequestBody @Valid CreateLessonSchemaRequest request, User user) {
		return ResponseEntity.ok(schemaService.create(
			LocalHtmlUtils.htmlEscape(request.getTitle(), 255),
			LocalHtmlUtils.htmlEscape(request.getDescription()),
			user
		));
	}

	@GetMapping("/{lid:[0-9]+}")
	public ResponseEntity<LessonSchema> getLessonSchema(@PathVariable long lid) {
		Optional<LessonSchema> byId = schemaService.getById(lid);
		if (byId.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(byId.get());
	}

	@GetMapping("/search")
	public ResponseEntity<Page<LessonSchema>> getByTitle(@RequestParam("word") List<String> searchWords,
														 @RequestParam(value = "uid", required = false) Long uid,
														 Pageable pageable) {
		Optional<User> creator = userService.getById(uid);
		List<String> escapedSearchWords = searchWords.stream()
			.map(LocalHtmlUtils::htmlEscape)
			.collect(Collectors.toList());
		if (creator.isEmpty()) {
			return ResponseEntity.ok(schemaService.getPage(escapedSearchWords, pageable));
		} else {
			return ResponseEntity.ok(schemaService.getPage(escapedSearchWords, creator.get(), pageable));
		}
	}

	@PutMapping("/{lid:[0-9]+}")
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

	@DeleteMapping("/{lid:[0-9]+}")
	public ResponseEntity<Void> delete(@PathVariable long lid) {
		Optional<LessonSchema> byId = schemaService.getById(lid);
		if (byId.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		schemaService.deleteSchema(byId.get());
		return ResponseEntity.ok().build();
	}
}
