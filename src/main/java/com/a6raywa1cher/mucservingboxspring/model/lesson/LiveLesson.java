package com.a6raywa1cher.mucservingboxspring.model.lesson;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@ToString(exclude = {"managedStudentPermissions", "creator"})
@EqualsAndHashCode(exclude = {"managedStudentPermissions", "creator"})
public class LiveLesson {
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@Column
	@JsonView(Views.Public.class)
	private String name;

	@ManyToOne
	@JsonView(Views.Public.class)
	private LessonSchema schema;

	@Column
	@JsonView(Views.Public.class)
	private ZonedDateTime startAt;

	@Column
	@JsonView(Views.Public.class)
	private ZonedDateTime endAt;

	@OneToOne
	@JsonView(Views.Public.class)
	private FSEntity root;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinTable
	@JsonView(Views.Internal.class)
	private List<FSEntityPermission> managedStudentPermissions = new ArrayList<>();

	@Column
	@JsonView(Views.Public.class)
	private ZonedDateTime createdAt;

	@ManyToOne
	@JsonView(Views.Public.class)
	@JsonIdentityReference(alwaysAsId = true)
	private User creator;

	@Transient
	@JsonInclude
	@JsonView(Views.Public.class)
	public LiveLessonStatus getStatus() {
		ZonedDateTime now = ZonedDateTime.now();
		if (now.isBefore(startAt)) {
			return LiveLessonStatus.SCHEDULED;
		} else if (endAt != null && now.isAfter(endAt)) {
			return LiveLessonStatus.ENDED;
		} else {
			return LiveLessonStatus.LIVE;
		}
	}

	@Transient
	@JsonInclude
	@JsonView(Views.Public.class)
	public List<User> getConnectedUsers() {
		return managedStudentPermissions.stream()
			.flatMap(p -> p.getAffectedUsers().stream())
			.collect(Collectors.toList());
	}
}
