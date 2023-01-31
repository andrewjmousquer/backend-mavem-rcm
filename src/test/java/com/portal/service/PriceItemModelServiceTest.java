package com.portal.service;

import static org.junit.Assert.assertNull;
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
import com.portal.dao.impl.PriceItemModelDAO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.ItemType;
import com.portal.model.Model;
import com.portal.model.PriceItemModel;
import com.portal.model.PriceList;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ItemModelService;
import com.portal.service.imp.PriceItemModelService;
import com.portal.service.imp.PriceListService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PriceItemModelServiceTest {

	@Mock
	PriceItemModelDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	PriceListService priceListService;

	@Mock
	BrandService brandService;
	
	@Mock
	ItemModelService itemModelService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PriceItemModelService service;

	private static final ItemType itemType = new ItemType(null, "ItemType 1", true, true, 1);
	private static final Item itemMock1 = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), itemType, null, null, null, null, null, null, null, null, null);
	private static final Brand brandMock = new Brand( 1, "BRAND 1", true );
	private static final Model modelMock1 = new Model( 1, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static final ItemModel itemModelMock1 = new ItemModel(1, 2000, 2015, itemMock1, modelMock1);
	private static final Channel channelMock = new Channel(1, "Channel 1", true, true, true);
	private static final PriceList priceListMock1 = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	
	@Nested
	class Save {
		@Test
		@DisplayName("Dado um ItemModelPrice sem nenhuma flag selecionada quando salva retorna o registro")
		void givenIMPWithFlagsFalse_whenSave_thenReturnId() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock1, itemModelMock1, null);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemModelService.getById( any() ) ).thenReturn( Optional.of( itemModelMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItemModel> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
			
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			
			assertNotNull( entityDB.get().getItemModel() );
			assertEquals( mock.getItemModel(), entityDB.get().getItemModel() );
			
			assertNull( entityDB.get().getBrand());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice sem nenhuma flag selecionado e sem ItemModel quando salva retornar erro")
		void givenIMPWithFlagsFalseAndNoItemModel_whenSave_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock1, null, null);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois o item por modelo relacionado é inválido ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice sem nenhuma flag selecionado e sem ItemModel quando salva retornar erro")
		void givenIMPWithFlagsFalseAndInvalidItemModel_whenSave_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock1, ItemModel.builder().id(1111).build(), null);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemModelService.getById( any() ) ).thenReturn( Optional.empty() );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois o item por modelo relacionado é inválido ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice com a flag TODAS AS MARCAS selecionado quando salva retornar o registro")
		void givenIMPWithAllBrandTrue_whenSave_thenReturnId() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, true , priceListMock1, itemMock1, null, null);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItemModel> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
			
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			
			assertNull( entityDB.get().getItemModel());
			assertNull( entityDB.get().getBrand());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS AS MARCAS selecionado e uma entidade marca quando salva retorna erro")
		void givenIMPWithAllBrandTrueAndBrand_whenSave_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, true , priceListMock1, itemMock1, null, brandMock);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é permitido salvar item por modelo ou marca quando a flag TODOS AS MARCAS está selecionada.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS AS MARCAS selecionado e uma entidade ItemModel quando salva retorna erro")
		void givenIMPWithAllBrandTrueAndItemModel_whenSave_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, true , priceListMock1, itemMock1, itemModelMock1, null);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é permitido salvar item por modelo ou marca quando a flag TODOS AS MARCAS está selecionada.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice com a flag TODAS OS MODELOS selecionado quando salva retornar o registro")
		void givenIMPWithAllModelsTrue_whenSave_thenReturnId() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, null, brandMock);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( brandService.getById( any() ) ).thenReturn( Optional.of( brandMock ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItemModel> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			assertNull( entityDB.get().getItemModel());
			assertNotNull( entityDB.get().getBrand() );
			assertEquals( mock.getBrand(), entityDB.get().getBrand() );
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS OS MODELOS selecionado e uma entidade modelo do item quando salva retorna erro")
		void givenIMPWithAllModelsTrueAndItemModel_whenSave_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, itemModelMock1, brandMock);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é permitido salvar o item por modelo quando a flag TODOS OS MODELOS está selecionada.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS OS MODELOS selecionado e uma entidade marca inválida quando salva retorna erro")
		void givenIMPWithAllModelsTrueAndInvalidBrand_whenSave_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, null, brandMock);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( brandService.getById( any() ) ).thenReturn( Optional.empty() );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois a marca relacionada é inválida ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS OS MODELOS selecionado e sem marca quando salva retorna erro")
		void givenIMPWithAllModelsTrueAndNoBrand_whenSave_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, null, null);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois a marca relacionada é inválida ou não existe.", e.getMessage());
		}
		
		@DisplayName("Salva um preço de item por modelo e da erro nos validators.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceItemModelServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidItemModelPrice_whenSave_thenTestValidador( PriceItemModel model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceItemModel>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Dado um ItemModelPrice sem nenhuma flag selecionada quando atualiza retorna o registro")
		void givenIMPWithFlagsFalse_whenUpdate_thenReturnId() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock1, itemModelMock1, null);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemModelService.getById( any() ) ).thenReturn( Optional.of( itemModelMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );

			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItemModel> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
			
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			
			assertNotNull( entityDB.get().getItemModel() );
			assertEquals( mock.getItemModel(), entityDB.get().getItemModel() );
			
			assertNull( entityDB.get().getBrand());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice sem nenhuma flag selecionado e sem ItemModel quando atualiza retornar erro")
		void givenIMPWithFlagsFalseAndNoItemModel_whenUpdate_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock1, null, null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois o item por modelo relacionado é inválido ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice sem nenhuma flag selecionado e sem ItemModel quando atualiza retornar erro")
		void givenIMPWithFlagsFalseAndInvalidItemModel_whenUpdate_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock1, ItemModel.builder().id(1111).build(), null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemModelService.getById( any() ) ).thenReturn( Optional.empty() );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois o item por modelo relacionado é inválido ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice com a flag TODAS AS MARCAS selecionado quando atualiza retornar o registro")
		void givenIMPWithAllBrandTrue_whenUpdate_thenReturnId() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, true , priceListMock1, itemMock1, null, null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItemModel> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
			
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			
			assertNull( entityDB.get().getItemModel());
			assertNull( entityDB.get().getBrand());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS AS MARCAS selecionado e uma entidade marca quando atualiza retorna erro")
		void givenIMPWithAllBrandTrueAndBrand_whenUpdate_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, true , priceListMock1, itemMock1, null, brandMock);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é permitido salvar item por modelo ou marca quando a flag TODOS AS MARCAS está selecionada.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS AS MARCAS selecionado e uma entidade ItemModel quando atualiza retorna erro")
		void givenIMPWithAllBrandTrueAndItemModel_whenUpdate_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, false, true , priceListMock1, itemMock1, itemModelMock1, null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é permitido salvar item por modelo ou marca quando a flag TODOS AS MARCAS está selecionada.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um ItemModelPrice com a flag TODAS OS MODELOS selecionado quando atualiza retornar o registro")
		void givenIMPWithAllModelsTrue_whenUpdate_thenReturnId() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, null, brandMock);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( brandService.getById( any() ) ).thenReturn( Optional.of( brandMock ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItemModel> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			assertNull( entityDB.get().getItemModel());
			assertNotNull( entityDB.get().getBrand() );
			assertEquals( mock.getBrand(), entityDB.get().getBrand() );
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS OS MODELOS selecionado e uma entidade modelo do item quando atualiza retorna erro")
		void givenIMPWithAllModelsTrueAndItemModel_whenUpdate_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, itemModelMock1, brandMock);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é permitido salvar o item por modelo quando a flag TODOS OS MODELOS está selecionada.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS OS MODELOS selecionado e uma entidade marca inválida quando atualiza retorna erro")
		void givenIMPWithAllModelsTrueAndInvalidBrand_whenUpdate_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, null, brandMock);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( brandService.getById( any() ) ).thenReturn( Optional.empty() );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois a marca relacionada é inválida ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Dado um IMP com a flag TODAS OS MODELOS selecionado e sem marca quando atualiza retorna erro")
		void givenIMPWithAllModelsTrueAndNoBrand_whenUpdate_thenError() throws Exception {
			PriceItemModel mock = new PriceItemModel(1, 100d, true, false , priceListMock1, itemMock1, null, null);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );

			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é possível salvar o relacionamento pois a marca relacionada é inválida ou não existe.", e.getMessage());
		}
		
		@DisplayName("Salva um preço de item por modelo e da erro nos validators.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceItemModelServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidItemModelPrice_whenUpdate_thenTestValidador( PriceItemModel model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceItemModel>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Dado um ID válido quanado deleta não retorna erro" )
		void givenIMPValid_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceItemModel() ) );
			when( dao.hasProposalDetailRelationship( anyInt() ) ).thenReturn( false );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Dado um ItemModelPrice inválido quando deleta deve dar erro" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Dado um ItemModelPrice inexistente quando deleta deve dar erro" )
		void givenNoExistedIMP_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O preço do item por modelo a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Dado um preço de item por modelo associado a uma proposta quando deleta ocorre erro" )
		void givenIMPWithProposalRelation_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceItemModel() ) );
			when( dao.hasProposalDetailRelationship( anyInt() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o preço do item por modelo pois existe um relacionamento com a proposta.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    		Arguments.of( new PriceItemModel(null, null, null, null, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(0, 100d, true, false , null, null, null, null) ),
    		Arguments.of( new PriceItemModel(null, 1d, true, true, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(null, 1d, true, null, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(null, 1d, null, null, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(null, null, true, true, priceListMock1, itemMock1, null, null) ),
    		Arguments.of( new PriceItemModel(null, null, null, true, priceListMock1, itemMock1, null, null) ),
    		Arguments.of( new PriceItemModel(null, null, null, null, priceListMock1, itemMock1, null, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
    		Arguments.of( new PriceItemModel(1, null, null, null, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(0, 100d, true, false , null, null, null, null) ),
    		Arguments.of( new PriceItemModel(null, 100d, true, false , null, null, null, null) ),
    		Arguments.of( new PriceItemModel(1, 1d, true, true, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(1, 1d, true, null, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(1, 1d, null, null, null, null, null, null) ),
    		Arguments.of( new PriceItemModel(1, null, true, true, priceListMock1, itemMock1, null, null) ),
    		Arguments.of( new PriceItemModel(1, null, null, true, priceListMock1, itemMock1, null, null) ),
    		Arguments.of( new PriceItemModel(1, null, null, null, priceListMock1, itemMock1, null, null) )
	    );
	}
}