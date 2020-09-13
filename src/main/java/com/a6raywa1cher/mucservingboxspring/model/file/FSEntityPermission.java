package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
	private List<FSEntity> entities = new ArrayList<>();

	@ManyToMany
	private List<User> affectedUsers = new ArrayList<>();

	@ElementCollection
	private List<UserRole> affectedUserRoles = new ArrayList<>();

	@Column(nullable = false)
	private Boolean allow;

	@Column(nullable = false)
	private Boolean applicationDefined;

	@Column(nullable = false)
	private Integer mask;
}
