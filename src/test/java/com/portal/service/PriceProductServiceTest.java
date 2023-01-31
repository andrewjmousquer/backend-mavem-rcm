package com.portal.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.portal.dao.impl.PriceProductDAO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Model;
import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.PriceListService;
import com.portal.service.imp.PriceProductService;
import com.portal.service.imp.ProductModelService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PriceProductServiceTest {

	@Mock
	PriceProductDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	PriceListService priceListService;
	
	@Mock
	ProductModelService productModelService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PriceProductService service;
	
	private static Channel channelMock = new Channel(1, "Channel 1", true, true, true);
	private static Brand brandMock = new Brand( 1, "BRAND 1", true );
	private static Model modelMock = new Model( 1, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static Product productMock = new Product( 1 , "PRODUCT 1", true, 10, null);
	private static PriceList priceListMock1 = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	private static ProductModel productModelMock1 = new ProductModel(1, false, 2000, 2015, 10, productMock, modelMock);
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo preço de produto válido e retorna a marca com ID")
		void givenValidProductPrice_whenSave_thenReturnId() throws Exception {

			PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( productModelService.getById( any() ) ).thenReturn( Optional.of( productModelMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceProduct> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			assertNotNull( entityDB.get().getProductModel() );
			assertEquals( mock.getProductModel(), entityDB.get().getProductModel() );
		}
		
		@DisplayName("Salva um preço de produto e da erro nos validators.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceProductServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidProductPrice_whenSave_thenTestValidador( PriceProduct model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceProduct>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo preço de produto duplicado.")
		void givenDuplicateProductPrice_whenSave_thenReturnError() throws Exception {
			PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);
			PriceProduct duplicateMock = new PriceProduct(2, 300d, priceListMock1, productModelMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( productModelService.getById( any() ) ).thenReturn( Optional.of( productModelMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateMock ) );
			
			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Já existe um preço definido para esse modelo na mesma lista.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um produto válido e retorna com a atualização")
		void givenProductPrice_whenUpdate_thenReturn() throws Exception {
			
			PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( productModelService.getById( any() ) ).thenReturn( Optional.of( productModelMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceProduct> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			assertNotNull( entityDB.get().getProductModel() );
			assertEquals( mock.getProductModel(), entityDB.get().getProductModel() );
		}
		
		@DisplayName("Atualiza um produto inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceProductServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidProductPrice_whenUpdate_thenTestValidador( PriceProduct model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceProduct>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo preço de produto duplicado")
		void givenDuplicateProductPrice_whenUpdate_thenReturnError() throws Exception {
			
			PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);
			PriceProduct duplicateMock = new PriceProduct(2, 300d, priceListMock1, productModelMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( productModelService.getById( any() ) ).thenReturn( Optional.of( productModelMock1 ) );
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceProduct() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateMock ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Já existe um preço definido para esse modelo na mesma lista.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um novo preço de produto existente e não pode dar erro de duplicado")
		void givenSelfProductPrice_whenUpdate_thenNoError() throws Exception {
			
			PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( productModelService.getById( any() ) ).thenReturn( Optional.of( productModelMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mock ) );
			
			assertDoesNotThrow( ()->service.update( mock, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um produto não existente")
		void givenNoExistProduct_whenUpdate_thenReturnError() throws Exception {
			
			PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "O preço do produto a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um preço de produto com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceProduct() ) );
			when( dao.hasProposalDetailRelationship( anyInt() ) ).thenReturn( false );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um produto com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um preço de produto com que não existe" )
		void givenNoExistedProductPrice_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O preço do produto a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Dado um preço de produto associado a uma proposta quando deleta ocorre erro" )
		void givenProductPriceWithProposalRelation_whenDelete_thenError() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceProduct() ) );
			when( dao.hasProposalDetailRelationship( anyInt() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o preço do produto pois existe um relacionamento com a proposta.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
			Arguments.of( new PriceProduct(null, null, null, null) ),
			Arguments.of( new PriceProduct(0, 1d, priceListMock1, productModelMock1) ),
			Arguments.of( new PriceProduct(null, 1d, null, null) ),
			Arguments.of( new PriceProduct(null, 1d, priceListMock1, null) ),
			Arguments.of( new PriceProduct(null, null, priceListMock1, productModelMock1) ),
			Arguments.of( new PriceProduct(null, null, null, productModelMock1) ),
			Arguments.of( new PriceProduct(null, 1d, null, productModelMock1) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
    		Arguments.of( new PriceProduct(1, null, null, null) ),
    		Arguments.of( new PriceProduct(0, 1d, priceListMock1, productModelMock1) ),
    		Arguments.of( new PriceProduct(null, 1d, priceListMock1, productModelMock1) ),
			Arguments.of( new PriceProduct(1, 1d, null, null) ),
			Arguments.of( new PriceProduct(1, 1d, priceListMock1, null) ),
			Arguments.of( new PriceProduct(1, null, priceListMock1, productModelMock1) ),
			Arguments.of( new PriceProduct(1, null, null, productModelMock1) ),
			Arguments.of( new PriceProduct(1, 1d, null, productModelMock1) )
	    );
	}
}
