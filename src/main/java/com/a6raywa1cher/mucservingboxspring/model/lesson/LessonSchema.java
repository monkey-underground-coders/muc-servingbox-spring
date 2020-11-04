package com.a6raywa1cher.mucservingboxspring.model.lesson;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = {"genericFiles", "creator", "liveLessons"})
@ToString(exclude = {"genericFiles", "creator", "liveLessons"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
	@JsonView(Views.Detailed.class)
	private List<LiveLesson> liveLessons;

	@Column
	@JsonView(Views.Public.class)
	private boolean onTheFly;
}
