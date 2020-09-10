package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class FSEntityPermission {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToMany
	private List<FSEntity> entities;

	@ManyToMany
	private List<User> affectedUsers;

	@ElementCollection
	private List<UserRole> affectedUserRoles;

	private boolean allow;

	private int mask;
}
