package com.a6raywa1cher.mucservingboxspring.model;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class User {
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

	@OneToMany(mappedBy = "creator")
	@JsonView(Views.Internal.class)
	private List<LessonSchema> schemaList = new ArrayList<>();

	@Column
	@JsonView(Views.Internal.class)
	private ZonedDateTime expiringAt;

	@Column
	@JsonView(Views.Public.class)
	private ZonedDateTime createdAt;

	@Column
	@JsonView(Views.Internal.class)
	private String createdIp;

	@Column
	@JsonView(Views.Internal.class)
	private ZonedDateTime lastVisitAt;

	@Transient
	@JsonView(Views.Public.class)
	public boolean isEnabled() {
		return expiringAt == null || ZonedDateTime.now().isBefore(expiringAt);
	}
}
