package com.a6raywa1cher.mucservingboxspring.rest.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProbeResponse {
	private boolean read;

	private boolean write;

	private boolean managePermissions;
}
