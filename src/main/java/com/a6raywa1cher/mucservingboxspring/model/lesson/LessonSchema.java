package com.a6raywa1cher.mucservingboxspring.model.lesson;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = {"genericFiles", "creator"})
@ToString(exclude = {"genericFiles", "creator"})
@JsonIdentityInfo(
	generator = ObjectIdGenerators.PropertyGenerator.class,
	property = "id")
public class LessonSchema {
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@Column
	@JsonView(Views.Public.class)
	private String title;

	@Lob
	@JsonView(Views.Public.class)
	private String description;

	@ManyToOne
	@JsonView(Views.Public.class)
	private User creator;

	@OneToOne
	@JsonView(Views.Public.class)
	private FSEntity genericFiles;

	@OneToMany(mappedBy = "schema")
	@JsonManagedReference
	private List<LiveLesson> liveLessons;

	@Column
	@JsonView(Views.Public.class)
	private boolean onTheFly;
}
