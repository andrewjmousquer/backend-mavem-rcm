package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.DocumentDAO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Classifier;
import com.portal.model.Document;
import com.portal.model.UserModel;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.DocumentService;
import com.portal.service.imp.ParameterService;
import com.portal.service.imp.UserService;

@ExtendWith(SpringExtension.class)
public class DocumentServiceTest {

	@Mock
	DocumentDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	UserService userService;
	
	@Mock
	ParameterService parameterService;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	DocumentService service;
	
	@Test
	void saveNewDocument() throws AppException, BusException {
		Document document = Document.builder()
									.filePath( System.getProperty("java.io.tmpdir") )
									.createDate( LocalDateTime.now() )
									.type( new Classifier(81) )
									.user( new UserModel( 1 ) )
									.build();
		
		when( userService.getById( any() ) ).thenReturn( Optional.of( new UserModel() ) );
		when( parameterService.getValueOf( any() ) ).thenReturn( "pdf,txt", "application/pdf,text/plain" );
		
		assertDoesNotThrow( ()->service.save( document , null) );
		
		// Deletamos para não deixar lixo e manter a sequencia de testes ok.
		new File( System.getProperty("java.io.tmpdir") + "/hello.txt" ).delete();
	}
	
	@Test
	void updateDocument() throws AppException, BusException {
		Document document = Document.builder()
									.id( 1 )
									.fileName( "hello.txt" )
									.filePath( System.getProperty("java.io.tmpdir") )
									.createDate( LocalDateTime.now() )
									.type( new Classifier(81) )
									.user( new UserModel( 1 ) )
									.build();
		
		when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Document() ) );
		when( userService.getById( any() ) ).thenReturn( Optional.of( new UserModel() ) );
		when( parameterService.getValueOf( any() ) ).thenReturn( "pdf,txt", "application/pdf,text/plain" );
		
		assertDoesNotThrow( ()->service.update( document , null) );
		
		// Deletamos para não deixar lixo e manter a sequencia de testes ok.
		new File( System.getProperty("java.io.tmpdir") + "/hello.txt" ).delete();
	}
	
	@Test
	void deleteDocument() throws AppException, BusException {
		Document documentToSave = Document.builder()
									.filePath( System.getProperty("java.io.tmpdir") )
									.createDate( LocalDateTime.now() )
									.type( new Classifier(81) )
									.user( new UserModel( 1 ) )
									.build();
		Document documentToDelete = Document.builder()
									.id( 1 )
									.fileName( "hello.txt" )
									.contentType( MediaType.TEXT_PLAIN_VALUE )
									.filePath( System.getProperty("java.io.tmpdir") )
									.createDate( LocalDateTime.now() )
									.type( new Classifier(81) )
									.user( new UserModel( 1 ) )
									.build();
		
		when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( documentToDelete ) );
		when( userService.getById( any() ) ).thenReturn( Optional.of( new UserModel() ) );
		when( parameterService.getValueOf( any() ) ).thenReturn( "pdf,txt", "application/pdf,text/plain" );
		
		assertDoesNotThrow( ()->service.save( documentToSave , null) );
		assertDoesNotThrow( ()->service.delete( 1 , null) );
	}
}
