package com.a6raywa1cher.mucservingboxspring.model.file;

import lombok.Data;

import java.io.Serializable;

@Data
public class FSEntityId implements Serializable {
	private Long id;

	private String path;
}
