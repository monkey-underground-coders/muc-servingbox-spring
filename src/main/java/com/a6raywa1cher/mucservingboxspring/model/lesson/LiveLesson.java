package com.a6raywa1cher.mucservingboxspring.model.lesson;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntity;
import com.a6raywa1cher.mucservingboxspring.model.file.FSEntityPermission;
import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
public class LiveLesson {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String name;

	@ManyToOne
	private LessonSchema schema;

	@Column
	private ZonedDateTime startAt;

	@Column
	private ZonedDateTime endAt;

	@OneToOne
	private FSEntity root;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable
	private List<FSEntityPermission> managedStudentPermissions = new ArrayList<>();

	@Column
	private ZonedDateTime createdAt;

	@ManyToOne
	private User creator;

	@Transient
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
	public List<User> getConnectedUsers() {
		return managedStudentPermissions.stream()
			.flatMap(p -> p.getAffectedUsers().stream())
			.collect(Collectors.toList());
	}
}
