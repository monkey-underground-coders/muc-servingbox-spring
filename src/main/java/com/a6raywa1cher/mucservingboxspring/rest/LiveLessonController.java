package com.a6raywa1cher.mucservingboxspring.rest;

import com.a6raywa1cher.mucservingboxspring.service.LessonSchemaService;
import com.a6raywa1cher.mucservingboxspring.service.LiveLessonService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/live")
public class LiveLessonController {
	private final LessonSchemaService schemaService;
	private final LiveLessonService liveLessonService;

	public LiveLessonController(LessonSchemaService schemaService, LiveLessonService liveLessonService) {
		this.schemaService = schemaService;
		this.liveLessonService = liveLessonService;
	}


}
