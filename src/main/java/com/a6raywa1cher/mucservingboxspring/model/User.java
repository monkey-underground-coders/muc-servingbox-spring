package com.a6raywa1cher.mucservingboxspring.model;

import com.a6raywa1cher.mucservingboxspring.model.lesson.LessonSchema;
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
	private Long id;

	@Column
	private String username;

	@Column
	private String password;

	@Column
	private String name;

	@Column
	private UserRole userRole;

	@OneToMany(mappedBy = "creator")
	private List<LessonSchema> schemaList = new ArrayList<>();

	@Column
	private ZonedDateTime expiringAt;

	@Column
	private ZonedDateTime createdAt;

	@Column
	private String createdIp;

	@Column
	private ZonedDateTime lastVisitAt;
}
