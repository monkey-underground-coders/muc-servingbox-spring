package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import com.a6raywa1cher.mucservingboxspring.utils.AlgorithmUtils;
import com.a6raywa1cher.mucservingboxspring.utils.Views;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

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
	private int pathLevel;

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
	@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
	@JsonIdentityReference(alwaysAsId = true)
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

	@Column
	@JsonView(Views.Public.class)
	private long maxSize;

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
		List<Integer> slashPositions = AlgorithmUtils.getSlashes(path);
		int slashes = slashPositions.size();
		return path.endsWith("/") ?
			path.substring(slashPositions.get(slashes - 2) + 1, path.length() - 1) :
			path.substring(slashPositions.get(slashes - 1) + 1);
	}

	@JsonIgnore
	public String getParentPath() {
		if (path == null) return null;
		if (path.equals("/")) return null;
		List<Integer> slashes = AlgorithmUtils.getSlashes(path);
		int size = slashes.size();
		if (isFolder) {
			if (size < 3) return null;
			return path.substring(0, slashes.get(size - 2) + 1);
		} else {
			if (size < 2) return null;
			return path.substring(0, slashes.get(size - 1) + 1);
		}
	}

	public static FSEntity createFile(String path, String diskObjectPath, long byteSize, User createdBy, boolean hidden) {
		Assert.isTrue(path.charAt(path.length() - 1) != '/', "The path can't contain the path separator at the end");
		return new FSEntity(null, path, (int) AlgorithmUtils.count(path, '/') + 1,
			false, diskObjectPath, hidden,
			createdBy, ZonedDateTime.now(), ZonedDateTime.now(),
			byteSize, -1);
	}

	public static FSEntity createFolder(String path, User createdBy, boolean hidden) {
		return createFolder(path, createdBy, hidden, -1);
	}

	public static FSEntity createFolder(String path, User createdBy, boolean hidden, long maxSize) {
		Assert.isTrue(path.charAt(path.length() - 1) == '/', "The path must contain the path separator at the end");
		return new FSEntity(null, path, (int) AlgorithmUtils.count(path, '/'),
			true, null, hidden,
			createdBy, ZonedDateTime.now(), ZonedDateTime.now(),
			0, maxSize);
	}
}