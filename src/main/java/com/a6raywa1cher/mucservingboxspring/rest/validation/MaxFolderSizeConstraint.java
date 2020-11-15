package com.a6raywa1cher.mucservingboxspring.rest.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MaxFolderSizeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxFolderSizeConstraint {
	String message() default "Invalid max size of the folder";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
