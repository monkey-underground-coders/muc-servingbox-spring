package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
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
public class FSEntityPermission {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToMany
	@JoinTable(
		indexes = {
			@Index(columnList = "entities_path"),
		}
	)
	private List<FSEntity> entities = new ArrayList<>();

	@ManyToMany
//	@JoinTable(
//		joinColumns = @JoinColumn(name = "fsentity_permission_id"),
//		inverseJoinColumns = @JoinColumn(name = "affected_users_id"),
//		indexes = {
//			@Index(columnList = "fsentity_permission_id, affected_users_id"),
//		}
//	)
	private List<User> affectedUsers = new ArrayList<>();

	@ElementCollection
	private List<UserRole> affectedUserRoles = new ArrayList<>();

	@Column(nullable = false)
	private Boolean applicationDefined;

	@Column(nullable = false)
	private Integer mask;

	@Transient
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
