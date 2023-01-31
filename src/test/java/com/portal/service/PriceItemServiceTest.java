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
import com.portal.dao.impl.PriceItemDAO;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemType;
import com.portal.model.PriceItem;
import com.portal.model.PriceList;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ItemService;
import com.portal.service.imp.PriceItemService;
import com.portal.service.imp.PriceListService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PriceItemServiceTest {

	@Mock
	PriceItemDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	PriceListService priceListService;
	
	@Mock
	ItemService itemService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PriceItemService service;
	private static final ItemType itemType = new ItemType(null, "ItemType 1", true, false, 1);
	private static final Item itemMock1 = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), itemType, null, null, null, null, null, null, null, null, null);
	private static Channel channelMock = new Channel(1, "Channel 1", true, true, true);
	private static PriceList priceListMock1 = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo preço de item válido e retorna a marca com ID")
		void givenValidItemPrice_whenSave_thenReturnId() throws Exception {
			PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemService.getById( any() ) ).thenReturn( Optional.of( itemMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItem> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			assertNotNull( entityDB.get().getItem() );
			assertEquals( mock.getItem(), entityDB.get().getItem() );
		}
		
		@DisplayName("Salva um preço de item e da erro nos validators.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceItemServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidItemPrice_whenSave_thenTestValidador( PriceItem model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceItem>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo preço de item duplicado.")
		void givenDuplicateItemPrice_whenSave_thenReturnError() throws Exception {
			PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);
			PriceItem duplicateMock = new PriceItem(2, 300d, itemMock1, priceListMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemService.getById( any() ) ).thenReturn( Optional.of( itemMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateMock ) );
			
			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Já existe um preço definido para esse item na mesma lista.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um item válido e retorna com a atualização")
		void givenItemPrice_whenUpdate_thenReturn() throws Exception {
			
			PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemService.getById( any() ) ).thenReturn( Optional.of( itemMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceItem> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getPrice(), entityDB.get().getPrice() );
			assertNotNull( entityDB.get().getPriceList() );
			assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
			assertNotNull( entityDB.get().getItem() );
			assertEquals( mock.getItem(), entityDB.get().getItem() );
		}
		
		@DisplayName("Atualiza um item inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceItemServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidItemPrice_whenUpdate_thenTestValidador( PriceItem model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceItem>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo preço de item duplicado")
		void givenDuplicateItemPrice_whenUpdate_thenReturnError() throws Exception {
			
			PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);
			PriceItem duplicateMock = new PriceItem(2, 300d, itemMock1, priceListMock1);
			
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemService.getById( any() ) ).thenReturn( Optional.of( itemMock1 ) );
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceItem() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateMock ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Já existe um preço definido para esse item na mesma lista.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um novo preço de item existente e não pode dar erro de duplicado")
		void givenSelfItemPrice_whenUpdate_thenNoError() throws Exception {
			
			PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( priceListMock1 ) );
			when( itemService.getById( any() ) ).thenReturn( Optional.of( itemMock1 ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mock ) );
			
			assertDoesNotThrow( ()->service.update( mock, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um item não existente")
		void givenNoExistProduct_whenUpdate_thenReturnError() throws Exception {
			
			PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "O preço do item a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um preço de item com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceItem() ) );
			when( dao.hasProposalDetailRelationship( anyInt() ) ).thenReturn( false );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um item com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um preço de item com que não existe" )
		void givenNoExistedItemPrice_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O preço do item a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Dado um preço de item associado a uma proposta quando deleta ocorre erro" )
		void givenItemPriceWithProposalRelation_whenDelete_thenError() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceItem() ) );
			when( dao.hasProposalDetailRelationship( anyInt() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o preço do item pois existe um relacionamento com a proposta.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
			Arguments.of( new PriceItem(null, null, null, null) ),
			Arguments.of( new PriceItem(1, 100d, itemMock1, priceListMock1) ),
			Arguments.of( new PriceItem(null, 1d, null, null) ),
			Arguments.of( new PriceItem(null, 1d, itemMock1, null) ),
			Arguments.of( new PriceItem(null, null, itemMock1, priceListMock1) ),
			Arguments.of( new PriceItem(null, null, null, priceListMock1) ),
			Arguments.of( new PriceItem(null, 1d, null, priceListMock1) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
    		Arguments.of( new PriceItem(1, null, null, null) ),
    		Arguments.of( new PriceItem(0, 100d, itemMock1, priceListMock1) ),
    		Arguments.of( new PriceItem(null, 100d, itemMock1, priceListMock1) ),
			Arguments.of( new PriceItem(1, 1d, null, null) ),
			Arguments.of( new PriceItem(1, 1d, itemMock1, null) ),
			Arguments.of( new PriceItem(1, null, itemMock1, priceListMock1) ),
			Arguments.of( new PriceItem(1, null, null, priceListMock1) ),
			Arguments.of( new PriceItem(1, 1d, null, priceListMock1) )
	    );
	}
}
