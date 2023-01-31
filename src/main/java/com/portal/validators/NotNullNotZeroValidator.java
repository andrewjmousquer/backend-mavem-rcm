package com.portal.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullNotZeroValidator implements ConstraintValidator<NotNullNotZero, Number> {

	@Override
	public void initialize(NotNullNotZero contactNumber) {
	}

	@Override
	public boolean isValid( Number field, ConstraintValidatorContext cxt ) {
		return field != null && !field.equals( 0 );
	}
}
