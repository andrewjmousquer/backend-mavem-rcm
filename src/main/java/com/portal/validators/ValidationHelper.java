package com.portal.validators;

import java.util.Set;

import javax.validation.ConstraintViolation;

import com.portal.exceptions.BusException;

public class ValidationHelper {

	/**
	 * Processa os erros de validações e gera uma exceção com o código E-4001.
	 * @throws BusException 
	 */
	public static <T> void generateException( Set<ConstraintViolation<T>> constraintViolations ) throws BusException {
		
		if( constraintViolations != null && !constraintViolations.isEmpty() ) {
			StringBuilder errors = new StringBuilder();
			
		    for( ConstraintViolation<?> violation : constraintViolations ) {
		    	errors.append( violation.getPropertyPath() + ": " + violation.getMessage() );
		    }
	
		    throw new BusException( errors.toString() );
		}
	}

	public interface OnUpdate {
	}
	
	public interface OnSave {
	}
}

