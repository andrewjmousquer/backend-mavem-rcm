package com.portal.exceptions;

public class StateWorkflowException extends Exception {

	private static final long serialVersionUID = -3376742936119717358L;

	public StateWorkflowException() {
	}

	public StateWorkflowException(String message) {
		super(message);
	}

	public StateWorkflowException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public StateWorkflowException( Throwable cause ) {
		super(cause);
	}
}
