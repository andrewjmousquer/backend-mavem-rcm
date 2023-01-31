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
import com.portal.dao.impl.ProductModelDAO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ModelService;
import com.portal.service.imp.ProductModelService;
import com.portal.service.imp.ProductService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class ProductModelServiceTest {

	@Mock
	ProductModelDAO dao;
	
	@Mock
	ProductService productService;
	
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
	ProductModelService service;

	private static Brand brandMock = new Brand( 1, "BRAND 1", true );
	private static Model modelMock1 = new Model( 1, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static Product productMock1 = new Product( 1 , "PRODUCT 1", true, 10, null);
	
	@Nested
	class Save {

		@DisplayName("Salva um novo relacionamento de produto e modelo inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ProductModelServiceTest#invalidEntityDataToSave" )
		void testEntityValidator( ProductModel entity ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<ProductModel>> violationSet = validator.validate( entity, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
		
		@Test
		@DisplayName("Dado um novo relacionamento de produto e modelo quando salvar retorna o ID")
		void givenProductModel_whenSave_thenReturnId() throws Exception {
			ProductModel mockDB = ProductModel.builder().id(1).build();
			ProductModel newProductModelMock = new ProductModel(null, false, 2000, 2015, 10, productMock1, modelMock1);
			
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.findDuplicated( any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mockDB ) );
			
			Optional<ProductModel> obj = service.save( newProductModelMock, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), mockDB.getId() );
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo inválido quando salva deve dar erro")
		void givenProductModelWithInvalidModel_whenSave_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(null, false, 2000, 2015, 10, productMock1, Model.builder().id(null).build());
			
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo não existente quando salva deve dar erro")
		void givenProductModelWithNoExistModel_whenSave_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(null, false, 2000, 2015, 10, productMock1, Model.builder().id(22).build());
			
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}

		@Test
		@DisplayName("Dado um relacionamento com o produto inválido quando salva deve dar erro")
		void givenProductModelWithInvalidProduct_whenSave_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(null, false, 2000, 2015, 10, Product.builder().id(null).build(), modelMock1);
			
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o produto relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o produto não existente quando salva deve dar erro")
		void givenProductModelWithNoExistProduct_whenSave_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(null, false, 2000, 2015, 10, Product.builder().id(22).build(), modelMock1);
			
			when( productService.getById( any() ) ).thenReturn( Optional.empty() );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o produto relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName( "Dado um novo relacionamento duplicado quando salvamos deve dar erro" )
		void giveNewRelationshipDuplicated_whenSave_thenError() throws AppException, BusException {
			
			ProductModel duplicateProductMock = new ProductModel(1, false, 2000, 2015, 10, productMock1, modelMock1);
			ProductModel newProductModelMock = new ProductModel(null, false, 2000, 2015, 10, productMock1, modelMock1);
			
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.findDuplicated( any() ) ).thenReturn( Arrays.asList(duplicateProductMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo.");
		}
	}
	
	@Nested
	class Update {
		
		@DisplayName("Atualiza um relacionamento de produto e modelo com dados inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ProductModelServiceTest#invalidEntityDataToUpdate" )
		void testEntityValidator( ProductModel partner ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<ProductModel>> violationSet = validator.validate( partner, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
		
		@Test
		@DisplayName("Atualiza um relacionamento de produto e modelo com sucesso e retorna o ID")
		void givenProductModel_whenUpdate_thenReturnId() throws Exception {
			ProductModel mockDB = ProductModel.builder().id(1).build();
			ProductModel productModelMock = new ProductModel(1, false, 2000, 2015, 10, productMock1, modelMock1);
			
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.findDuplicated( any() ) ).thenReturn( null );
			when( dao.getById( any() ) ).thenReturn( Optional.of( productModelMock ) );
			when( dao.update( any() ) ).thenReturn( Optional.of( mockDB ) );
			
			Optional<ProductModel> obj = service.update( productModelMock, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), mockDB.getId() );
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo inválido quando atualiza deve dar erro")
		void givenProductModelWithInvalidModel_whenUpdate_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(1, false, 2000, 2015, 10, productMock1, Model.builder().id(null).build());
			
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newProductModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o modelo não existente quando atualiza deve dar erro")
		void givenProductModelWithNoExistModel_whenUpdate_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(1, false, 2000, 2015, 10, productMock1, Model.builder().id(22).build());
			
			when( modelService.getById( any() ) ).thenReturn( Optional.empty() );
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newProductModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe.");
		}

		@Test
		@DisplayName("Dado um relacionamento com o produto inválido quando atualiza deve dar erro")
		void givenProductModelWithInvalidProduct_whenUpdate_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(1, false, 2000, 2015, 10, Product.builder().id(null).build(), modelMock1);
			
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newProductModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o produto relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName("Dado um relacionamento com o produto não existente quando atualiza deve dar erro")
		void givenProductModelWithNoExistProduct_whenUpdate_thenError() throws Exception {
			ProductModel newProductModelMock = new ProductModel(1, false, 2000, 2015, 10, Product.builder().id(22).build(), modelMock1);
			
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( productService.getById( any() ) ).thenReturn( Optional.empty() );
			when( dao.getById( any() ) ).thenReturn( Optional.of(newProductModelMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(newProductModelMock, null));
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento pois o produto relacionado é inválido ou não existe.");
		}
		
		@Test
		@DisplayName( "Dado um relacionamento duplicado quando atualizamos deve dar erro" )
		void givenProductModelDuplicate_whenUpdate_thenError() throws AppException, BusException {
			ProductModel duplicateProductMock = new ProductModel(2, false, 2000, 2015, 10, productMock1, modelMock1);
			ProductModel productModelMock = new ProductModel(1, false, 2000, 2015, 10, productMock1, modelMock1);
			
			when( productService.getById( any() ) ).thenReturn( Optional.of(productMock1) );
			when( modelService.getById( any() ) ).thenReturn( Optional.of(modelMock1) );
			when( dao.getById( any() ) ).thenReturn( Optional.of(productModelMock) );
			when( dao.findDuplicated( any() ) ).thenReturn( Arrays.asList(duplicateProductMock) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(productModelMock, null));
			assertEquals( ex.getMessage(), "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo.");;
		}
		
		@Test
		@DisplayName( "Dado um relacionamento de produto e modelo que não existe quando atualizamos deve dar erro" )
		void givenNonexistentProductModel_whenUpdate_thenError() throws AppException, BusException {
			ProductModel productModelMock = new ProductModel(22, false, 2000, 2015, 10, productMock1, modelMock1);

			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(productModelMock, null));
			assertEquals( ex.getMessage(), "O produto a ser atualizado não existe." );
		}
		
	}
	
	@Nested
	class Get {
		@Test
		@DisplayName("Dado um ID de relacionamento de produto e modelo válido quando busca por ID retorna a entidade")
		void givenProductModelID_whenGetById_thenReturnId() throws Exception {
			ProductModel productModelMock = new ProductModel(1, false, 2000, 2015, 10, productMock1, modelMock1);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( productModelMock ) );
			
			Optional<ProductModel> entityDB = service.getById( productModelMock.getId() );

			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( productModelMock, entityDB.get() );
			assertEquals( productModelMock, entityDB.get() );
			assertEquals( productModelMock.getHasProject(), entityDB.get().getHasProject() );
			assertEquals( productModelMock.getModelYearStart(), entityDB.get().getModelYearStart());
			assertEquals( productModelMock.getModelYearEnd(), entityDB.get().getModelYearEnd());
			assertEquals( productModelMock.getManufactureDays(), entityDB.get().getManufactureDays() );
			assertNotNull( entityDB.get().getModel() );
			assertEquals( productModelMock.getModel(), entityDB.get().getModel() );
			assertNotNull( entityDB.get().getProduct() );
			assertEquals( productModelMock.getProduct(), entityDB.get().getProduct() );
		}
		
		@Test
		@DisplayName("Dado um ID de relacionamento de produto e modelo inválido quando busca por ID retorna erro")
		void givenInvalidProductModelID_whenGetById_thenReturnId() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getById( null ));
			assertEquals( ex.getMessage(), "ID de busca inválido." );
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um relacionamento de produto e modelo com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			ProductModel productModelMock = new ProductModel(1, false, 2000, 2015, 10, productMock1, modelMock1);
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( productModelMock ) );
			assertDoesNotThrow( ()->service.delete(productModelMock.getId(), null) );
		}
		
		@Test
		@DisplayName( "Delete um relacionamento de produto e modelo com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um relacionamento de produto e modelo com que não existe" )
		void givenNoExistedProductModel_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O produto a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um relacionamento de produto e modelo com relacionamento com lista de preço e retorna erro" )
		void givenGroupInProductModelRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new ProductModel() ) );
			when( dao.hasPriceListRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o relacionamento de produto com modelo pois existe um relacionamento com lista de preço.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSave() {
	    return Stream.of(
			Arguments.of( new ProductModel(null, null, null, null, null, null, null) ),
			Arguments.of( new ProductModel(0, false, 2000, 2000, 30, new Product(), null) ),
			Arguments.of( new ProductModel(null, false, null, null, null, null, null) ),
			Arguments.of( new ProductModel(null, false, 2000, null, null, null, null) ),
			Arguments.of( new ProductModel(null, false, 2000, 2000, null, null, null) ),
			Arguments.of( new ProductModel(null, false, 2000, 2000, 30, null, null) ),
			Arguments.of( new ProductModel(null, false, 2000, 2000, 30, new Product(), null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdate() {
	    return Stream.of(
    		Arguments.of( new ProductModel(1, null, null, null, null, null, null) ),
    		Arguments.of( new ProductModel(0, false, 2000, 2000, 30, new Product(), null) ),
    		Arguments.of( new ProductModel(null, false, 2000, 2000, 30, new Product(), null) ),
			Arguments.of( new ProductModel(1, false, null, null, null, null, null) ),
			Arguments.of( new ProductModel(1, false, 2000, null, null, null, null) ),
			Arguments.of( new ProductModel(1, false, 2000, 2000, null, null, null) ),
			Arguments.of( new ProductModel(1, false, 2000, 2000, 30, null, null) ),
			Arguments.of( new ProductModel(1, false, 2000, 2000, 30, new Product(), null) )
	    );
	}
}
