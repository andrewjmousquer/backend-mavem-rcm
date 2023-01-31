package com.portal.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.portal.dao.impl.PartnerGroupDAO;
import com.portal.exceptions.BusException;
import com.portal.model.PartnerGroup;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.PartnerGroupService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)
public class PartnerGroupServiceTest {

	@Mock
	PartnerGroupDAO dao;

	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PartnerGroupService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os partners groups e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.PartnerGroupServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnGroupList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new PartnerGroup() ) );

			List<PartnerGroup> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo grupo válido e retorna a marca com ID")
		void givenValidPartnerGroup_whenSave_thenReturnId() throws Exception {
			PartnerGroup mock = new PartnerGroup( 1, "PartnerGroup 1", true );
			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PartnerGroup> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getName(), entityDB.get().getName() );
			assertEquals( mock.getActive(), entityDB.get().getActive() );
		}
		
		@DisplayName("Salva um grupo e da erro nos validators. PTG-I1")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PartnerGroupServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidPartnerGroup_whenSave_thenTestValidador( PartnerGroup model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PartnerGroup>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo grupo duplicada com o mesmo nome. PTG-I2")
		void givenDuplicatePartnerGroup_whenSave_thenReturnError_PTGI2() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( PartnerGroup.builder().id( 1 ).build() ) );
			
			PartnerGroup model = PartnerGroup.builder()
								.name( "PartnerGroup 1" )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um grupo com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um grupo válido e retorna com a atualização")
		void givenPartnerGroup_whenUpdate_thenReturnNewPartnerGroup() throws Exception {
			PartnerGroup mock = new PartnerGroup( 1, "PartnerGroup 1", true );
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PartnerGroup() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PartnerGroup> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getName(), entityDB.get().getName() );
			assertEquals( mock.getActive(), entityDB.get().getActive() );
		}
		
		@DisplayName("Atualiza um grupo inválido e retorna erro. PTG-U1")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PartnerGroupServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidPartnerGroup_whenUpdate_thenTestValidador( PartnerGroup model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PartnerGroup>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo grupo duplicada com o mesmo nome. PTG-U2")
		void givenDuplicatePartnerGroup_whenUpdate_thenReturnError_PTGU2() throws Exception {
			
			PartnerGroup model = PartnerGroup.builder()
					.id( 1 )
					.name( "PartnerGroup 1" )
					.build();
			
			PartnerGroup duplicateModel = PartnerGroup.builder()
					.id( 2 )
					.name( "PartnerGroup 1" )
					.build();
			
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PartnerGroup() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um grupo com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza uma grupo de parceiros existente e não pode dar erro de duplicado")
		void givenSelfPartnerGroup_whenUpdate_thenNoError() throws Exception {
			
			PartnerGroup model = PartnerGroup.builder()
												.id( 1 )
												.name( "PartnerGroup 1" )
												.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PartnerGroup() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um grupo não existente. PTG-U3")
		void givenNoExistModel_whenUpdate_thenReturnError_PTGU3() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			PartnerGroup model = PartnerGroup.builder()
								.id( 1 )
								.name( "PartnerGroup 1" )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O grupo a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um grupo com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PartnerGroup() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um grupo com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um grupo que não existe" )
		void givenNoExistedGroup_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O grupo a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um grupo com relacionamento com parceiro e retorna erro" )
		void givenGroupInPartnerRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new PartnerGroup() ) );
			when( dao.hasPartnerRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o grupo pois existe um relacionamento com parceiro.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
			Arguments.of(0, 1, "DESC", "id"),
			Arguments.of(0, 1, "DESC", null),
			Arguments.of(0, 1, "DESC", "ptg_id"),
			Arguments.of(0, 1, null, "id"),
			Arguments.of(0, 0, "DESC", "id"),
			Arguments.of(0, 0, "DESC", "id"),
			Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
			Arguments.of( new PartnerGroup(null, null, null) ),
			Arguments.of( new PartnerGroup(1, "Partner Group", true) ),
			Arguments.of( new PartnerGroup(null, null, true) ),
			Arguments.of( new PartnerGroup(null, "Partner Group", null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
			Arguments.of( new PartnerGroup(1, null, null) ),
			Arguments.of( new PartnerGroup(0, "Partner Group", true) ),
			Arguments.of( new PartnerGroup(null, "Partner Group", true) ),
			Arguments.of( new PartnerGroup(1, null, true) ),
			Arguments.of( new PartnerGroup(1, "Partner Group", null) )
	    );
	}
}
