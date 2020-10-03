package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
@ToString(exclude = "createdBy")
@IdClass(FSEntityId.class)
@NoArgsConstructor
@AllArgsConstructor
public class FSEntity {
	@Id
	@GeneratedValue
	private Long id;

	@Id
	@Column(length = 512)
	private String path;

	@Column
	private Boolean isFolder;

	@Column(length = 1024)
	private String diskObjectPath;

	@Column
	private Boolean hidden;

	@ManyToOne
	private User createdBy;

	@Column
	private ZonedDateTime createdTimestamp;

	@Column
	private ZonedDateTime modifiedTimestamp;

	@Column
	private long byteSize;

	public boolean isFile() {
		return isFolder != null && !isFolder;
	}

	public boolean isFolder() {
		return isFolder != null && isFolder;
	}

	public static FSEntity createFile(String path, String diskObjectPath, long byteSize, User createdBy, boolean hidden) {
		return new FSEntity(null, path, false, diskObjectPath, hidden, createdBy, ZonedDateTime.now(), ZonedDateTime.now(), byteSize);
	}

	public static FSEntity createFolder(String path, User createdBy, boolean hidden) {
		return new FSEntity(null, path, true, null, hidden, createdBy, ZonedDateTime.now(), ZonedDateTime.now(), 0);
	}
}