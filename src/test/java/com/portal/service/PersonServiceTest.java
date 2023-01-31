package com.portal.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.portal.dao.impl.PersonDAO;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Classifier;
import com.portal.model.Person;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ContactService;
import com.portal.service.imp.PersonService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PersonServiceTest {

	@Mock
	PersonDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ContactService contactService;
	
	@Mock
	ClassifierService classifierService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PersonService service;

	@Nested
	class ListAll {
		@DisplayName("Listar as pessoas e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.PersonServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnPersonList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Person() ) );

			List<Person> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@ParameterizedTest
		@DisplayName("Salva uma pessoa e da erro nos validators. PRD-I1, PRD-I3")
		@MethodSource( "com.portal.service.PersonServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidPerson_whenSave_thenTestValidador( Person model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Person>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
		
		@ParameterizedTest
		@DisplayName("Salva uma nova pessoa válida e retorna a marca com ID")
		@MethodSource( "com.portal.service.PersonServiceTest#validEntityToSave" )
		void givenValidPerson_whenSave_thenReturnId( Person model, Person result ) throws Exception {
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( result ) );
			when( classifierService.find( any() ) ).thenReturn( Optional.of( new Classifier( 1 ) ) );
			when( classifierService.getById( any() ) ).thenReturn( Optional.of( new Classifier( 1 ) ) );
			
			Optional<Person> obj = service.save( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), result.getId() );
		}
	}
	
	@Nested
	class Update {
		
		@ParameterizedTest
		@DisplayName("Atualiza uma pessoa válida e retorna com a atualização")
		@MethodSource( "com.portal.service.PersonServiceTest#validEntityToUpdate" )
		void givenPerson_whenUpdate_thenReturnNewPerson( Person model ) throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Person() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( classifierService.find( any() ) ).thenReturn( Optional.of( new Classifier( 1 ) ) );
			when( classifierService.getById( any() ) ).thenReturn( Optional.of( new Classifier( 1 ) ) );
			when( dao.update( any() ) ).thenReturn( Optional.of( model ) );
			
			Optional<Person> obj = service.update( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( model, obj.get() );
			assertEquals( model.getId(), obj.get().getId() );
			assertEquals( model.getName(), obj.get().getName() );
			assertEquals( model.getJobTitle(), obj.get().getJobTitle() );
			assertEquals( model.getCpf(), obj.get().getCpf() );
			assertEquals( model.getCnpj(), obj.get().getCnpj() );
			assertEquals( model.getRg(), obj.get().getRg() );
			assertEquals( model.getRne(), obj.get().getRne() );
		}
		
		@DisplayName("Atualiza uma pessoa inválido e retorna erro. PRD-U1, PRD-U3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PersonServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidPerson_whenUpdate_thenTestValidador( Person model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Person>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza uma pessoa não existente. PRD-U4")
		void givenNoExistPerson_whenUpdate_thenReturnError_PRDU4() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			Person model = Person.builder()
								.id( 1 )
								.name( "Person 1" )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "A pessoa a ser atualizada não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Get {
		@Test
		@DisplayName( "Dado um ID de uma pessoa válida então retorna o objeto." )
		void givenValidPersonId_whenGetById_thenReturnEntity() throws AppException, BusException {
			
			Person mock = new Person( 1, "Person 1", "Job 1", "0000000000001", "00000000000001", "00000000001", "00000000000000000001", null, null );
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Person> db = service.getById( mock.getId() );
			
			assertNotNull( db );
			assertTrue( db.isPresent() );
			assertEquals( mock, db.get() );
			assertEquals( mock.getId(), db.get().getId() );
			assertEquals( mock.getName(), db.get().getName() );
			assertEquals( mock.getJobTitle(), db.get().getJobTitle() );
			assertEquals( mock.getCpf(), db.get().getCpf() );
			assertEquals( mock.getCnpj(), db.get().getCnpj() );
			assertEquals( mock.getRg(), db.get().getRg() );
			assertEquals( mock.getRne(), db.get().getRne() );
			assertEquals( mock.getRne(), db.get().getRne() );
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "per_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> validEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Person( null, "Person 1", "Job 1", "0000000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() ), Person.builder().id(1).build() ),
    		Arguments.of( new Person( null, "Person 2", "Job 2", "00000000002", "00000000000002", "00000000002", null, null, PersonClassification.PF.getType() ), Person.builder().id(2).build() ),
    		Arguments.of( new Person( null, "Person 3", "Job 3", "00000000003", "00000000000003", null, null, null, PersonClassification.PF.getType() ), Person.builder().id(3).build() ),
    		Arguments.of( new Person( null, "Person 4", "Job 4", "00000000004", null, null, null, null, PersonClassification.PF.getType() ), Person.builder().id(4).build() ),
    		Arguments.of( new Person( null, "Person 5", "Job 5", null, null, null, null, null, PersonClassification.PF.getType() ), Person.builder().id(5).build() ),
    		Arguments.of( new Person( null, "Person 6",  null, null, null, null, null, null, PersonClassification.PF.getType() ), Person.builder().id(6).build() )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> validEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Person( 1, "Person 1.1", "Job 1.1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 2, "Person 2.1", "Job 2.1", "00000000002", "00000000000002", "00000000002", null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 3, "Person 3.1", "Job 3.1", "00000000003", "00000000000003", null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 4, "Person 4.1", "Job 4.1", "00000000004", null, null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 5, "Person 5.1", "Job 5.1", null, null, null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 6, "Person 6.1",  null, null, null, null, null, null, PersonClassification.PF.getType() ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
	    	Arguments.of( new Person( null, null, null ) ),
	    	Arguments.of( new Person( 0, "Person 1", null ) ),
	    	Arguments.of( new Person( null, "Person 1", null, "0000000000012", null, null, null, null, null ) ),
	    	Arguments.of( new Person( null, "Person 1", null, null, "000000000000014", null, null, null, null ) ),
	    	Arguments.of( new Person( null, "Person 1", null, null, null, "0000000000000000000000000000000000000000000045", null, null, null ) ),
	    	Arguments.of( new Person( null, "Person 1", null, null, null, null, "000000000000000000020", null, null ) ),
	    	Arguments.of( new Person( null, null, null, "0000000000012", null, null, null, null, null ) ),
	    	Arguments.of( new Person( null, null, null, null, "000000000000014", null, null, null, null ) ),
	    	Arguments.of( new Person( null, null, null, null, null, "0000000000000000000000000000000000000000000045", null, null, null ) ),
	    	Arguments.of( new Person( null, null, null, null, null, null, "000000000000000000020", null, null ) ),
	    	Arguments.of( new Person( null, null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, "Person 1", null, "0000000000012", null, null, null, null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, "Person 1", null, null, "000000000000014", null, null, null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, "Person 1", null, null, null, "0000000000000000000000000000000000000000000045", null, null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, "Person 1", null, null, null, null, "000000000000000000020", null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, null, null, "0000000000012", null, null, null, null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, null, null, null, "000000000000014", null, null, null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, null, null, null, null, "0000000000000000000000000000000000000000000045", null, null, PersonClassification.PF.getType() ) ),
	    	Arguments.of( new Person( null, null, null, null, null, null, "000000000000000000020", null, PersonClassification.PF.getType() ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
		return Stream.of(
				Arguments.of( new Person( 1, null, null ) ),
				Arguments.of( new Person( 0, "Person 1", null ) ),
				Arguments.of( new Person( null, "Person 1", null ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, "0000000000012", null, null, null, null, null ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, null, "000000000000014", null, null, null, null ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, null, null, "0000000000000000000000000000000000000000000045", null, null, null ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, null, null, null, "000000000000000000020", null, null ) ),
		    	Arguments.of( new Person( 1, null, null, "0000000000012", null, null, null, null, null ) ),
		    	Arguments.of( new Person( 1, null, null, null, "000000000000014", null, null, null, null ) ),
		    	Arguments.of( new Person( 1, null, null, null, null, "0000000000000000000000000000000000000000000045", null, null, null ) ),
		    	Arguments.of( new Person( 1, null, null, null, null, null, "000000000000000000020", null, null ) ),
		    	Arguments.of( new Person( 1, null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, "0000000000012", null, null, null, null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, null, "000000000000014", null, null, null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, null, null, "0000000000000000000000000000000000000000000045", null, null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, "Person 1", null, null, null, null, "000000000000000000020", null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, null, null, "0000000000012", null, null, null, null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, null, null, null, "000000000000014", null, null, null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, null, null, null, null, "0000000000000000000000000000000000000000000045", null, null, PersonClassification.PF.getType() ) ),
		    	Arguments.of( new Person( 1, null, null, null, null, null, "000000000000000000020", null, PersonClassification.PF.getType() ) )
		    );
	}
}
