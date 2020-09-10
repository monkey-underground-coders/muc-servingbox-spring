package com.a6raywa1cher.mucservingboxspring.model.lesson;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
public class LiveLesson {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private LessonSchema schema;

	@Column
	private ZonedDateTime startAt;

	@Column
	private ZonedDateTime endAt;

	@OneToOne
	private FSEntity root;

	@Column
	private ZonedDateTime createdAt;

	@ManyToOne
	private User creator;
}
