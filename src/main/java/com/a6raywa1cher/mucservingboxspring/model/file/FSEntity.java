package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Entity
@Data
@ToString(exclude = "createdBy")
@NoArgsConstructor
@AllArgsConstructor
public class FSEntity {
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@Column(length = 512, unique = true)
	@JsonView(Views.Public.class)
	private String path;

	@Column
	@JsonView(Views.Public.class)
	private Boolean isFolder;

	@Column(length = 1024)
	@JsonView(Views.Public.class)
	private String diskObjectPath;

	@Column
	@JsonView(Views.Public.class)
	private Boolean hidden;

	@ManyToOne
	@JsonView(Views.Public.class)
	private User createdBy;

	@Column
	@JsonView(Views.Public.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private ZonedDateTime createdTimestamp;

	@Column
	@JsonView(Views.Public.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private ZonedDateTime modifiedTimestamp;

	@Column
	@JsonView(Views.Public.class)
	private long byteSize;

	@Transient
	@JsonIgnore
	public boolean isFile() {
		return isFolder != null && !isFolder;
	}

	@Transient
	@JsonIgnore
	public boolean isFolder() {
		return isFolder != null && isFolder;
	}

	@JsonInclude
	@JsonView(Views.Public.class)
	public String getName() {
		if (path == null) {
			return null;
		}
		List<Integer> slashPositions = IntStream.range(0, path.length())
			.filter(i -> path.charAt(i) == '/')
			.boxed()
			.collect(Collectors.toList());
		int slashes = slashPositions.size();
		return path.endsWith("/") ?
			path.substring(slashPositions.get(slashes - 2) + 1, path.length() - 1) :
			path.substring(slashPositions.get(slashes - 1) + 1);
	}

	public static FSEntity createFile(String path, String diskObjectPath, long byteSize, User createdBy, boolean hidden) {
		return new FSEntity(null, path, false, diskObjectPath, hidden, createdBy, ZonedDateTime.now(), ZonedDateTime.now(), byteSize);
	}

	public static FSEntity createFolder(String path, User createdBy, boolean hidden) {
		return new FSEntity(null, path, true, null, hidden, createdBy, ZonedDateTime.now(), ZonedDateTime.now(), 0);
	}
}