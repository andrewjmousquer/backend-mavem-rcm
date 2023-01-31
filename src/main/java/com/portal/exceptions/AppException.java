package com.portal.exceptions;

public class AppException extends Exception {

	private static final long serialVersionUID = 3907773148752201468L;

	public AppException() {
	}

	public AppException( String message ) {
		super(message);
	}
	
	public AppException(String message, Exception e) {
		super(message);
		if(e!=null) e.printStackTrace();
	}

	public AppException(Throwable cause) {
		super(cause);
	}

	public AppException(String message, Throwable cause) {
		super(message, cause);
	}
}
