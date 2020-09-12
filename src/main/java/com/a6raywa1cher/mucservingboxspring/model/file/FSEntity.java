package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
@ToString(exclude = "createdBy")
public class FSEntity {
	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 1024)
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
}