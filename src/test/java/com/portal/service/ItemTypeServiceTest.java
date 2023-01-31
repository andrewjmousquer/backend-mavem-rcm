package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import com.portal.dao.impl.ItemTypeDAO;
import com.portal.exceptions.BusException;
import com.portal.model.ItemType;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ItemTypeService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class ItemTypeServiceTest {

	@Mock
	ItemTypeDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	ItemTypeService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os itens type e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.ItemTypeServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnItemTypeList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new ItemType() ) );

			List<ItemType> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo item type válido e retorna a marca com ID")
		void givenValidItemType_whenSave_thenReturnId() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( ItemType.builder().id(1).build() ) );
			
			ItemType model = ItemType.builder()
								.name( "ItemType 1" )
								.mandatory( true )
								.multi( false )
								.seq( 0 )
								.build();
			
			Optional<ItemType> obj = service.save( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), new Integer(1) );
		}
		
		@DisplayName("Salva um item type e da erro nos validators. ITT-I1, ITT-I3, ITT-I4, ITT-I5 ")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ItemTypeServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidItemType_whenSave_thenTestValidador( ItemType model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<ItemType>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo item type duplicado com o mesmo nome. ITT-I2")
		void givenDuplicateItemType_whenSave_thenReturnError_ITTI2() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( ItemType.builder().id( 1 ).build() ) );
			
			ItemType model = ItemType.builder()
								.name( "ItemType 1" )
								.mandatory( true )
								.multi( false )
								.seq( 0 )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um tipo de item com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um item type válido e retorna com a atualização")
		void givenItemType_whenUpdate_thenReturnNewItemType() throws Exception {
			
			ItemType model = ItemType.builder()
								.id( 1 )
								.name( "ItemType 1.1" )
								.mandatory( false )
								.multi( true )
								.seq( 1 )
								.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new ItemType() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( model ) );
			
			Optional<ItemType> obj = service.update( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), model.getId());
			assertEquals( obj.get().getName(), model.getName());
			assertEquals( obj.get().getMandatory(), model.getMandatory());
			assertEquals( obj.get().getMulti(), model.getMulti());
			assertEquals( obj.get().getSeq(), model.getSeq());
		}
		
		@DisplayName("Atualiza um item type inválido e retorna erro. ITT-U1, ITT-U3, ITT-U4, ITT-U5 ")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ItemTypeServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidItemType_whenUpdate_thenTestValidador( ItemType model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<ItemType>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo item type duplicado com o mesmo nome. ITT-U2")
		void givenDuplicateItemType_whenUpdate_thenReturnError_ITTU2() throws Exception {
			
			ItemType model = ItemType.builder()
								.id( 1 )
								.name( "ItemType 1" )
								.mandatory( false )
								.multi( true )
								.seq( 1 )
								.build();
			
			ItemType duplicateModel = ItemType.builder()
									.id( 2 )
									.name( "ItemType 1" )
									.mandatory( false )
									.multi( true )
									.seq( 1 )
									.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new ItemType() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um tipo de item com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um novo tipo de item existente e não pode dar erro de duplicado")
		void givenSelfItemType_whenUpdate_thenNoError() throws Exception {
			
			ItemType model = ItemType.builder()
									.id( 1 )
									.name( "ItemType 1" )
									.mandatory( false )
									.multi( true )
									.seq( 1 )
									.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new ItemType() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um item type não existente. ITT-U6")
		void givenNoExistItemType_whenUpdate_thenReturnError_ITTU6() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			ItemType model = ItemType.builder()
								.id( 1 )
								.name( "ItemType 1.1" )
								.mandatory( false )
								.multi( true )
								.seq( 1 )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O tipo de item a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete um item type com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new ItemType() ) );
			when( dao.hasItemRelationship( any() ) ).thenReturn( false );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um item type com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um item type com que não existe" )
		void givenNoExistedItemType_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O tipo de item a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um tipo de item com relacionamento com item" )
		void givenItemType_whenDelete_thenRelationshipItemError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( ItemType.builder().id(1).build() ) );
			when( dao.hasItemRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir o tipo de item pois existe um relacionamento com item.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "itt_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    			Arguments.of( new ItemType(null, null, null, null, null) ),
    			Arguments.of( new ItemType(0, "ItemType 1", true, false, 0) ),
    			Arguments.of( new ItemType(null, "ItemType 1", null, null, null) ),
    			Arguments.of( new ItemType(null, "ItemType 1", true, null, null) ),
    			Arguments.of( new ItemType(null, "ItemType 1", true, true, null) ),
    			Arguments.of( new ItemType(null, "ItemType 1", true, null, 0) ),
    			Arguments.of( new ItemType(null, "ItemType 1", null, true, null) ),
    			Arguments.of( new ItemType(null, "ItemType 1", null, true, 0) ),
    			Arguments.of( new ItemType(null, "ItemType 1", null, null, 0) ),
    			Arguments.of( new ItemType(null, null, true, null, null) ),
    			Arguments.of( new ItemType(null, null, true, true, null) ),
    			Arguments.of( new ItemType(null, null, true, null, 0) ),
    			Arguments.of( new ItemType(null, null, null, true, null) ),
    			Arguments.of( new ItemType(null, null, null, true, 0) ),
    			Arguments.of( new ItemType(null, null, null, null, 0))
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new ItemType(1, null, null, null, null) ),
	    		Arguments.of( new ItemType(0, "ItemType 1", true, true, 0) ),
	    		Arguments.of( new ItemType(null, "ItemType 1", true, true, 0) ),
    			Arguments.of( new ItemType(1, "ItemType 1", null, null, null) ),
    			Arguments.of( new ItemType(1, "ItemType 1", true, null, null) ),
    			Arguments.of( new ItemType(1, "ItemType 1", true, true, null) ),
    			Arguments.of( new ItemType(1, "ItemType 1", true, null, 0) ),
    			Arguments.of( new ItemType(1, "ItemType 1", null, true, null) ),
    			Arguments.of( new ItemType(1, "ItemType 1", null, true, 0) ),
    			Arguments.of( new ItemType(1, "ItemType 1", null, null, 0) ),
    			Arguments.of( new ItemType(1, null, true, null, null ),
    			Arguments.of( new ItemType(1, null, true, true, null) ),
    			Arguments.of( new ItemType(1, null, true, null, 0) ),
    			Arguments.of( new ItemType(1, null, null, true, null) ),
    			Arguments.of( new ItemType(1, null, null, true, 0) ),
    			Arguments.of( new ItemType(1, null, null, null, 0) ))
	    );
	}
}
