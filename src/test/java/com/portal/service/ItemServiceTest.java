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
import com.portal.dao.impl.ItemDAO;
import com.portal.exceptions.BusException;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemType;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ItemModelService;
import com.portal.service.imp.ItemService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class ItemServiceTest {

	@Mock
	ItemDAO dao;
	
	@Mock
	ItemModelService itemModelService;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	ItemService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os itens e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.ItemServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnItemList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Item() ) );
			List<Item> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo item válido e retorna a marca com ID")
		void givenValidItem_whenSave_thenReturnId() throws Exception {
			Item mockCheck = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);

			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mockCheck ) );
			
			Optional<Item> modelDB = service.save( mockCheck, null );
			
			assertTrue( modelDB.isPresent() );
			assertEquals( mockCheck, modelDB.get() );
			assertEquals( mockCheck.getId(), modelDB.get().getId() );
			assertEquals( mockCheck.getName(), modelDB.get().getName() );
			assertEquals( mockCheck.getCod(), modelDB.get().getCod() );
			assertEquals( mockCheck.getSeq(), modelDB.get().getSeq() );
			assertEquals( mockCheck.getForFree(), modelDB.get().getForFree() );
			assertEquals( mockCheck.getGeneric(), modelDB.get().getGeneric() );
			assertEquals( mockCheck.getIcon(), modelDB.get().getIcon() );
			assertNotNull( modelDB.get().getItemType() );
			assertEquals( mockCheck.getItemType(), modelDB.get().getItemType() );
			assertNotNull( modelDB.get().getMandatory() );
			assertEquals( mockCheck.getMandatory(), modelDB.get().getMandatory() );
		}
		
		@DisplayName("Salva um item e da erro nos validators")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ItemServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidItem_whenSave_thenTestValidador( Item model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
		    Set<ConstraintViolation<Item>> violationSet = validator.validate( model, OnSave.class );
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo item duplicado com o mesmo nome.")
		void givenDuplicateItem_whenSave_thenReturnError() throws Exception {
			Item mockCheck = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
			Item duplicateMock = new Item(2, "Item 2", "220", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);

			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateMock ) );
			
			BusException e = assertThrows( BusException.class, ()->service.save( mockCheck, null ) );
			assertEquals( "Já existe um item com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um item válido e retorna com a atualização")
		void givenItem_whenUpdate_thenReturnNewItem() throws Exception {
			Item mockCheck = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Item() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mockCheck ) );
			
			Optional<Item> modelDB = service.update( mockCheck, null );
			
			assertTrue( modelDB.isPresent() );
			assertEquals( mockCheck, modelDB.get() );
			assertEquals( mockCheck.getId(), modelDB.get().getId() );
			assertEquals( mockCheck.getName(), modelDB.get().getName() );
			assertEquals( mockCheck.getCod(), modelDB.get().getCod() );
			assertEquals( mockCheck.getSeq(), modelDB.get().getSeq() );
			assertEquals( mockCheck.getForFree(), modelDB.get().getForFree() );
			assertEquals( mockCheck.getGeneric(), modelDB.get().getGeneric() );
			assertNotNull( modelDB.get().getItemType() );
			assertEquals( mockCheck.getItemType(), modelDB.get().getItemType() );
			assertNotNull( modelDB.get().getMandatory() );
			assertEquals( mockCheck.getMandatory(), modelDB.get().getMandatory() );
		}
		
		@DisplayName("Atualiza um item inválido e retorna erro. ITM-U1, ITM-U3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ItemServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidItem_whenUpdate_thenTestValidador( Item model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Item>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo item duplicado com o mesmo nome")
		void givenDuplicateItem_whenUpdate_thenReturnError_ITMU2() throws Exception {
			Item model = new Item(2, "Item 2", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
			Item duplicateModel = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Item() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um item com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um item existente e não pode dar erro de duplicado")
		void givenSelfItem_whenUpdate_thenNoError() throws Exception {
			Item model = new Item(2, "Item 2", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Item() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um item não existente.")
		void givenNoExistItem_whenUpdate_thenReturnError_ITMU4() throws Exception {
			
			Item model = new Item(200, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O item a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um item com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Item() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um item com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um item com que não existe" )
		void givenNoExistedItem_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O item a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um grupo com relacionamento com parceiro e retorna erro" )
		void givenItemInPriceListRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new Item() ) );
			when( dao.hasPriceItemRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o item pois existe um relacionamento com lista de preço.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "itm_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    		Arguments.of( new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null))
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
		return Stream.of(
    		Arguments.of( new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null))
	    );
	}
}