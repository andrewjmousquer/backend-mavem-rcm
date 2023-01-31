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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.ItemModelDAO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.ItemType;
import com.portal.model.Model;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ItemModelService;
import com.portal.service.imp.ItemService;
import com.portal.service.imp.ModelService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class ItemModelServiceTest {

	@Mock
	ItemModelDAO dao;
	
	@Mock
	ItemService itemService;
	
	@Mock
	ModelService modelService;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	ItemModelService service;

	private static Brand brandMock = new Brand( 1, "BRAND 1", true );
	private static Model modelMock1 = new Model( 1, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static ItemType itemTypeMock = new ItemType( 1, "ItemType 1", true, false, 1 );
	private static Item itemMock1 = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), itemTypeMock, null, null, null, null, null, null, null, null, null);
	
	@Nested
	class Save {

		@DisplayName("Salva um novo relacionamento de item e modelo inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ItemModelServiceTest#invalidEntityDataToSave" )
		void testEntityValidator( ItemModel entity ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<ItemModel>> violationSet = validator.validate( entity, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
		
		@Test
		@DisplayName("Dado um novo relacionamento de item e modelo quando salvar retorna o ID")
		void givenItemModel_whenSave_thenReturnId() throws Exception {
			ItemModel mockDB = ItemModel.builder().id(1).build();
			ItemModel newItemModelMock = new ItemModel(null, 2000, 2015, itemMock1, modelMock1);
			
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.findDuplicated( any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mockDB ) );
			
			Optional<ItemModel> obj = service.save( newItemModelMock, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), mockDB.getId() );
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo inválido quando salva deve dar erro")
		void givenItemModelWithInvalidModel_whenSave_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(null, 2000, 2015, itemMock1, Model.builder().id(null).build());
			
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo não existente quando salva deve dar erro")
		void givenItemModelWithNoExistModel_whenSave_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(null, 2000, 2015, itemMock1, Model.builder().id(22).build());
			
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}

		@Test
		@DisplayName("Dado um relacionamento com o item inválido quando salva deve dar erro")
		void givenItemModelWithInvalidItem_whenSave_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(null, 2000, 2015, Item.builder().id(null).build(), modelMock1);
			
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o item relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o item não existente quando salva deve dar erro")
		void givenItemModelWithNoExistItem_whenSave_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(null, 2000, 2015, Item.builder().id(22).build(), modelMock1);
			
			when( itemService.getById( any() ) ).thenReturn( Optional.empty() );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o item relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName( "Dado um novo relacionamento duplicado quando salvamos deve dar erro" )
		void giveNewRelationshipDuplicated_whenSave_thenError() throws AppException, BusException {
			
			ItemModel duplicateProductMock = new ItemModel(1, 2000, 2015, itemMock1, modelMock1);
			ItemModel newItemModelMock = new ItemModel(null, 2000, 2015, itemMock1, modelMock1);
			
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.findDuplicated( any() ) ).thenReturn( Arrays.asList(duplicateProductMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Já existe um relacionamento com esse item e modelo, dentro do mesmo range de ano/modelo.");
		}
	}
	
	@Nested
	class Update {
		
		@DisplayName("Atualiza um relacionamento de item e modelo com dados inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ItemModelServiceTest#invalidEntityDataToUpdate" )
		void testEntityValidator( ItemModel partner ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<ItemModel>> violationSet = validator.validate( partner, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
		
		@Test
		@DisplayName("Atualiza um relacionamento de item e modelo com sucesso e retorna o ID")
		void givenItemModel_whenUpdate_thenReturnId() throws Exception {
			ItemModel mockDB = ItemModel.builder().id(1).build();
			ItemModel productModelMock = new ItemModel(1, 2000, 2015, itemMock1, modelMock1);
			
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.findDuplicated( any() ) ).thenReturn( null );
			when( dao.getById( any() ) ).thenReturn( Optional.of( productModelMock ) );
			when( dao.update( any() ) ).thenReturn( Optional.of( mockDB ) );
			
			Optional<ItemModel> obj = service.update( productModelMock, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), mockDB.getId() );
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo inválido quando atualiza deve dar erro")
		void givenItemModelWithInvalidModel_whenUpdate_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(1, 2000, 2015, itemMock1, Model.builder().id(null).build());
			
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newItemModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo não existente quando atualiza deve dar erro")
		void givenItemModelWithNoExistModel_whenUpdate_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(1, 2000, 2015, itemMock1, Model.builder().id(22).build());
			
			when( modelService.getById( any() ) ).thenReturn( Optional.empty() );
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newItemModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}

		@Test
		@DisplayName("Dado um relacionamento com o item inválido quando atualiza deve dar erro")
		void givenItemModelWithInvalidItem_whenUpdate_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(1, 2000, 2015, Item.builder().id(null).build(), modelMock1);
			
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newItemModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o item relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o item não existente quando atualiza deve dar erro")
		void givenItemModelWithNoExistItem_whenUpdate_thenError() throws Exception {
			ItemModel newItemModelMock = new ItemModel(1, 2000, 2015, Item.builder().id(22).build(), modelMock1);
			
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( itemService.getById( any() ) ).thenReturn( Optional.empty() );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newItemModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newItemModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o item relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName( "Dado um relacionamento duplicado quando atualizamos deve dar erro" )
		void givenItemModelDuplicate_whenUpdate_thenError() throws AppException, BusException {
			ItemModel duplicateProductMock = new ItemModel(2, 2000, 2015, itemMock1, modelMock1);
			ItemModel productModelMock = new ItemModel(1, 2000, 2015, itemMock1, modelMock1);
			
			when( itemService.getById( any() ) ).thenReturn( Optional.of(itemMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(productModelMock) );
			when( dao.findDuplicated( any() ) ).thenReturn( Arrays.asList(duplicateProductMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(productModelMock, null));
			assertEquals( ex.getMessage(), "Já existe um relacionamento com esse item e modelo, dentro do mesmo range de ano/modelo.");;
		}
		
		@Test
		@DisplayName( "Dado um relacionamento de item e modelo que não existe quando atualizamos deve dar erro" )
		void givenNonexistentItemModel_whenUpdate_thenError() throws AppException, BusException {
			ItemModel productModelMock = new ItemModel(22, 2000, 2015, itemMock1, modelMock1);

			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(productModelMock, null));
			assertEquals( ex.getMessage(), "O modelo do item a ser atualizado não existe." );
		}
		
	}
	
	@Nested
	class Get {
		@Test
		@DisplayName("Dado um ID de relacionamento de item e modelo válido quando busca por ID retorna a entidade")
		void givenItemModelID_whenGetById_thenReturnId() throws Exception {
			ItemModel productModelMock = new ItemModel(1, 2000, 2015, itemMock1, modelMock1);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( productModelMock ) );
			
			Optional<ItemModel> entityDB = service.getById( productModelMock.getId() );

			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( productModelMock, entityDB.get() );
			assertEquals( productModelMock, entityDB.get() );
			assertEquals( productModelMock.getModelYearStart(), entityDB.get().getModelYearStart());
			assertEquals( productModelMock.getModelYearEnd(), entityDB.get().getModelYearEnd());
			assertNotNull( entityDB.get().getModel() );
			assertEquals( productModelMock.getModel(), entityDB.get().getModel() );
			assertNotNull( entityDB.get().getItem() );
			assertEquals( productModelMock.getItem(), entityDB.get().getItem() );
		}
		
		@Test
		@DisplayName("Dado um ID de relacionamento de item e modelo inválido quando busca por ID retorna erro")
		void givenInvalidItemModelID_whenGetById_thenReturnId() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getById( null ));
			assertEquals( ex.getMessage(), "ID de busca inválido." );
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um relacionamento de item e modelo com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			ItemModel productModelMock = new ItemModel(1, 2000, 2015, itemMock1, modelMock1);
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( productModelMock ) );
			assertDoesNotThrow( ()->service.delete(productModelMock.getId(), null) );
		}
		
		@Test
		@DisplayName( "Delete um relacionamento de item e modelo com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um relacionamento de item e modelo com que não existe" )
		void givenNoExistedItemModel_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O modelo do item a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um relacionamento de item e modelo com relacionamento com lista de preço e retorna erro" )
		void givenGroupInItemModelRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new ItemModel() ) );
			when( dao.hasPriceListRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o relacionamento de item com modelo pois existe um relacionamento com lista de preço.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSave() {
	    return Stream.of(
			Arguments.of( new ItemModel(null, null, null, null, null) ),
			Arguments.of( new ItemModel(0, 2000, 2015, itemMock1, modelMock1) ),
			Arguments.of( new ItemModel(null, 2000, null, null, null) ),
			Arguments.of( new ItemModel(null, 2000, 2000, null, null) ),
			Arguments.of( new ItemModel(null, 2000, 2000, null, null) ),
			Arguments.of( new ItemModel(null, 2000, 2000, new Item(), null) ),
			Arguments.of( new ItemModel(null, null, 2000, new Item(), new Model()) ),
			Arguments.of( new ItemModel(null, null, null, new Item(), new Model()) ),
			Arguments.of( new ItemModel(null, null, null, null, new Model()) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdate() {
	    return Stream.of(
    		Arguments.of( new ItemModel(1, null, null, null, null) ),
    		Arguments.of( new ItemModel(0, 2000, 2015, itemMock1, modelMock1) ),
    		Arguments.of( new ItemModel(null, 2000, 2015, itemMock1, modelMock1) ),
			Arguments.of( new ItemModel(1, 2000, null, null, null) ),
			Arguments.of( new ItemModel(1, 2000, 2000, null, null) ),
			Arguments.of( new ItemModel(1, 2000, 2000, null, null) ),
			Arguments.of( new ItemModel(1, 2000, 2000, new Item(), null) ),
			Arguments.of( new ItemModel(1, null, 2000, new Item(), new Model()) ),
			Arguments.of( new ItemModel(1, null, null, new Item(), new Model()) ),
			Arguments.of( new ItemModel(1, null, null, null, new Model()) )
	    );
	}
}
