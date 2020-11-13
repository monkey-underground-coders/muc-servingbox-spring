package com.a6raywa1cher.mucservingboxspring.model.lesson;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.Type;

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
	@JsonView(Views.Detailed.class)
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@ManyToOne
	@JsonView(Views.Public.class)
	private User creator;

	@OneToOne
	@JsonView(Views.Public.class)
	private FSEntity genericFiles;

	@OneToMany(mappedBy = "schema")
	@JsonView(Views.Detailed.class)
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	private List<LiveLesson> liveLessons;

	@Column
	@JsonView(Views.Public.class)
	private boolean onTheFly;
}
