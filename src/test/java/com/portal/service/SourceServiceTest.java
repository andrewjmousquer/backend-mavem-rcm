package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.SourceDAO;
import com.portal.exceptions.BusException;
import com.portal.model.Source;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.SourceService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class SourceServiceTest {

	@Mock
	SourceDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	SourceService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar as fontes e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.SourceServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnSourceList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Source() ) );

			List<Source> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva uma novo fonte válido e retorna a marca com ID")
		void givenValidSource_whenSave_thenReturnId() throws Exception {
			
			Source mock = Source.builder()
									.id( 1 )
									.name( "Source 1" )
									.active( true )
									.build();

			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Source> entity = service.save( mock, null );
			
			assertNotNull( entity );
			assertTrue( entity.isPresent() );
			assertEquals( entity.get(), mock );
			assertEquals( mock.getName(), entity.get().getName());
			assertEquals( mock.getActive(), entity.get().getActive());
		}
		
		@DisplayName("Salva um fonte e da erro nos validators")
		@ParameterizedTest
		@MethodSource( "com.portal.service.SourceServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidSource_whenSave_thenTestValidador( Source model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Source>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva uma nova fonte duplicada com o mesmo nome")
		void givenDuplicateSource_whenSave_thenReturnError() throws Exception {
			
			Source mock = Source.builder()
					.id( 1 )
					.name( "Source 1" )
					.active( true )
					.build();

			Source duplicateModel = Source.builder()
									.id( 2 )
									.name( "Source 1" )
									.active( true )
									.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Source() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Já existe uma fonte com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza uma fonte válido e retorna com a atualização")
		void givenSource_whenUpdate_thenReturnNewSource() throws Exception {
			
			Source mock = Source.builder()
									.id( 1 )
									.name( "Source 1.1" )
									.active( false )
									.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Source() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Source> entity = service.update( mock, null );
			
			assertNotNull( entity );
			assertTrue( entity.isPresent() );
			assertEquals( entity.get(), mock );
			assertEquals( mock.getName(), entity.get().getName());
			assertEquals( mock.getActive(), entity.get().getActive());
		}
		
		@DisplayName("Atualiza uma fonte inválido e retorna erro. CHN-U1, CHN-U3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.SourceServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidSource_whenUpdate_thenTestValidador( Source model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Source>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza uma nova fonte duplicada com o mesmo nome")
		void givenDuplicateSource_whenUpdate_thenReturnError() throws Exception {

			Source mock = Source.builder()
									.id( 1 )
									.name( "Source 1" )
									.active( true )
									.build();
			
			Source duplicateModel = Source.builder()
										.id( 2 )
										.name( "Source 1" )
										.active( true )
										.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Source() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Já existe uma fonte com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um fonte existente e não pode dar erro de duplicado")
		void givenSelfSource_whenUpdate_thenNoError() throws Exception {
			
			Source model = Source.builder()
									.id( 1 )
									.name( "Source 1" )
									.active( true )
									.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Source() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza uma fonte não existente")
		void givenNoExistSource_whenUpdate_thenReturnError_CHNU4() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			Source model = Source.builder()
										.id( 1 )
										.name( "Source 1" )
										.active( true )
										.build();
					
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "A fonte a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um fonte com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Source() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Deleta uma fonte com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Deleta uma fonte com que não existe" )
		void givenNoExistedCanal_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "A fonte a ser excluída não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Deleta uma fonte com relacionamento com lead e retorna erro" )
		void givenSourceInPartnerRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new Source() ) );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir a fonte pois existe um relacionamento com lead.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "src_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    			Arguments.of( new Source(null, null, null) ),
    			Arguments.of( new Source(0, "Source 1", true) ),
    			Arguments.of( new Source(null, "Source 1", null) ),
    			Arguments.of( new Source(null, null, true) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new Source(1, null, null) ),
	    		Arguments.of( new Source(0, "Source 1", true) ),
	    		Arguments.of( new Source(null, "Source 1", true) ),
    			Arguments.of( new Source(1, "Source 1", null) ),
    			Arguments.of( new Source(1, null, true) )
	    );
	}
}
