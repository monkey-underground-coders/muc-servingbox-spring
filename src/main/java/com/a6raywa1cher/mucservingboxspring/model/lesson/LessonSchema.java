package com.a6raywa1cher.mucservingboxspring.model.lesson;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class LessonSchema {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String title;

	@Column
	private String description;

	@ManyToOne
	private User creator;

	@OneToOne
	private FSEntity genericFiles;

	@Column
	private boolean onTheFly;
}
