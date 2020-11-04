package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Data
@Table(indexes = {
	@Index(columnList = "mask")
})
@AllArgsConstructor
@Builder
public class FSEntityPermission {

	@OneToOne(optional = false)
	@JsonView(Views.Public.class)
	private FSEntity entity;

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;
	@ManyToMany(fetch = FetchType.EAGER)
	@JsonView(Views.Public.class)
	private List<User> affectedUsers;
	@ElementCollection
	@JsonView(Views.Public.class)
	private List<UserRole> affectedUserRoles;

	public FSEntityPermission() {
		affectedUsers = new ArrayList<>();
		affectedUserRoles = new ArrayList<>();
	}

	@Column(nullable = false)
	@JsonView(Views.Public.class)
	private Boolean applicationDefined;

	@Column(nullable = false)
	@JsonView(Views.Public.class)
	private Integer mask;

	@Transient
	@JsonInclude
	public List<ActionType> getActionTypes() {
		int mask = this.getMask();
		return Stream.of(ActionType.values())
			.filter(t -> (mask & t.mask) > 0)
			.collect(Collectors.toList());
	}

	@Transient
	public void setActionTypes(List<ActionType> actionTypes) {
		setMask(actionTypes.stream().reduce(0, (i, a) -> i | a.mask, (i1, i2) -> i1 | i2));
	}
}
