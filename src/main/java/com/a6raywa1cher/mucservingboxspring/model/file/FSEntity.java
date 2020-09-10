package com.a6raywa1cher.mucservingboxspring.model.file;

import com.a6raywa1cher.mucservingboxspring.model.User;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

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
	private String name;

	@Column
	private Boolean isFolder;

	@Column(length = 1024)
	private String diskObjectPath;

	@ManyToOne
	private User createdBy;

	@Column
	private LocalDateTime createdTimestamp;

	@Column
	private LocalDateTime modifiedTimestamp;

	@Column
	private long byteSize;

	public boolean isFile() {
		return isFolder != null && !isFolder;
	}

	public boolean isFolder() {
		return isFolder != null && isFolder;
	}
}