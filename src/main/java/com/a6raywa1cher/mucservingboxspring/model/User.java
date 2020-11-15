package com.a6raywa1cher.mucservingboxspring.model;

import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = {"schemaList", "rootFolder"})
@ToString(exclude = {"schemaList", "rootFolder"})
@AllArgsConstructor
@Builder
public class User {
	@OneToMany(mappedBy = "creator")
	@JsonView(Views.Internal.class)
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	private List<LessonSchema> schemaList;

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@Column
	@JsonView(Views.Internal.class)
	private String username;

	@Column
	@JsonIgnore
	private String password;

	@Column
	@JsonView(Views.Public.class)
	private String name;

	@Column
	@JsonView(Views.Public.class)
	private UserRole userRole;

	public User() {
		schemaList = new ArrayList<>();
	}

	@Column
	@JsonView(Views.Internal.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private ZonedDateTime expiringAt;

	@Column
	@JsonView(Views.Public.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private ZonedDateTime createdAt;

	@Column
	@JsonView(Views.Internal.class)
	private String createdIp;

	@Column
	@JsonView(Views.Internal.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private ZonedDateTime lastVisitAt;

	@OneToOne
	@JsonView(Views.Internal.class)
	private FSEntity rootFolder;

	@Transient
	@JsonView(Views.Public.class)
	public boolean isEnabled() {
		return expiringAt == null || ZonedDateTime.now().isBefore(expiringAt);
	}
}
