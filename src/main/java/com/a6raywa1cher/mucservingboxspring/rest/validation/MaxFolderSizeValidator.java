package com.a6raywa1cher.mucservingboxspring.rest.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MaxFolderSizeValidator implements ConstraintValidator<MaxFolderSizeConstraint, Long> {
	public void initialize(MaxFolderSizeConstraint constraint) {
	}

	public boolean isValid(Long obj, ConstraintValidatorContext context) {
		return obj == -1 || obj >= 0;
	}
}
