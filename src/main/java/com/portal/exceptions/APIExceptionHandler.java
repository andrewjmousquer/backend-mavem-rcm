package com.portal.exceptions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class APIExceptionHandler extends ResponseEntityExceptionHandler {
    
	@ExceptionHandler(value = AppException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<APIErrorResponse> applicationException(AppException e) {
		e.printStackTrace();
		APIErrorResponse error = APIErrorResponse.builder()
										.message( e.getMessage() )
										.status( HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() )
										.statusCode( HttpStatus.INTERNAL_SERVER_ERROR.value() )
										.timeStamp( LocalDateTime.now(ZoneOffset.UTC) )
										.build();

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(value = BusException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<APIErrorResponse> businessException(BusException e) {
		e.printStackTrace();
		APIErrorResponse error = APIErrorResponse.builder()
										.message( e.getMessage() )
										.status( BusException.CODE_REASON )
										.statusCode( BusException.CODE )
										.timeStamp( LocalDateTime.now(ZoneOffset.UTC) )
										.properties( e.getProperty("error") )
										.build();

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	
	@ExceptionHandler(value = Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<APIErrorResponse> generalException(Exception e) {
		e.printStackTrace();
		APIErrorResponse error = APIErrorResponse.builder()
										.message( "Unknown error" )
										.status( HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() )
										.statusCode( HttpStatus.INTERNAL_SERVER_ERROR.value() )
										.timeStamp( LocalDateTime.now(ZoneOffset.UTC) )
										.build();

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}