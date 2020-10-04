package com.a6raywa1cher.mucservingboxspring.rest.req;

import com.a6raywa1cher.mucservingboxspring.model.UserRole;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class CreateUserRequest {
	@NotBlank
	@Pattern(regexp = "[a-zA-Z_\\-0-9]{5,25}")
	private String username;

	@NotBlank
	@Size(min = 1, max = 255)
	private String password;

	@NotBlank
	@Size(max = 50)
	private String name;

	@NotNull
	private UserRole userRole;
}
