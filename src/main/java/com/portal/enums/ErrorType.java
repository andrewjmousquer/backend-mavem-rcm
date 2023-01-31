package com.portal.enums;

public enum ErrorType  {

	APPLICATION_ERROR( 1000 ),
	
	BUSINESS_ERROR( 1001 ),
	
	GENERIC_ERROR( 1002 ),
	
	JWT_ERROR( 1003 );
	
	private Integer code;
	
	private ErrorType( Integer code ) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
