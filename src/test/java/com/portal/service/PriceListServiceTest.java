package com.portal.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.portal.dao.impl.PriceListDAO;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.model.PriceList;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.PriceItemModelService;
import com.portal.service.imp.PriceItemService;
import com.portal.service.imp.PriceListPartnerService;
import com.portal.service.imp.PriceListService;
import com.portal.service.imp.PriceProductService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PriceListServiceTest {

	@Mock
	PriceListDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	PriceListPartnerService partnerPriceListService; 
	
	@Mock
	PriceProductService productPriceService;
	
	@Mock
	PriceItemService itemPriceService;
	
	@Mock
	PriceItemModelService itemModelPriceService;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PriceListService service;
	
	private static final Channel channel = new Channel(1, "Channel 1", true, true, true);
	private static final Channel channel2 = new Channel(1, "Channel 2", true, true, true);
	
	@Nested
	class ListAll {
		@DisplayName("Listar os lista de preços e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.PriceListServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnPriceList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new PriceList() ) );

			List<PriceList> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva uma nova lista de preço válido e retorna a lista com ID")
		void givenValidPriceList_whenSave_thenReturnId() throws Exception {
			PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceList> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( entityDB.get().getName(), mock.getName());
			assertEquals( entityDB.get().getStart(), mock.getStart());
			assertEquals( entityDB.get().getEnd(), mock.getEnd());
			assertNotNull( entityDB.get().getChannel() );
			assertEquals( entityDB.get().getChannel(), mock.getChannel());
			assertEquals( entityDB.get().getChannel().getName(), mock.getChannel().getName());
			assertEquals( entityDB.get().getChannel().getActive(), mock.getChannel().getActive());
		}
		
		@DisplayName("Salva um lista de preço e da erro nos validators. PRL-I1, PRL-I2, PRL-I3, PRL-I4")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceListServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidPriceList_whenSave_thenTestValidador( PriceList model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceList>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo lista de preço duplicado com o mesmo nome. PRL-I5")
		void givenDuplicatePriceList_whenSave_thenReturnError() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( PriceList.builder().id( 1 ).build() ) );

			PriceList mock = new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
			
			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Já existe uma lista de preço com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um lista de preço válido e retorna com a atualização")
		void givenPriceList_whenUpdate_thenReturnNewPriceList() throws Exception {
			
			PriceList mock = new PriceList(1, "PriceList 1.1", LocalDateTime.of(2022, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 12, 31, 00, 00, 00, 00), channel2, false);
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceList() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceList> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( entityDB.get().getName(), mock.getName());
			assertEquals( entityDB.get().getStart(), mock.getStart());
			assertEquals( entityDB.get().getEnd(), mock.getEnd());
			assertNotNull( entityDB.get().getChannel() );
			assertEquals( entityDB.get().getChannel(), mock.getChannel());
			assertEquals( entityDB.get().getChannel().getName(), mock.getChannel().getName());
			assertEquals( entityDB.get().getChannel().getActive(), mock.getChannel().getActive());
		}
		
		@DisplayName("Atualiza um lista de preço inválido e retorna erro. PRL-I1, PRL-I2, PRL-I3, PRL-I4")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PriceListServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidPriceList_whenUpdate_thenTestValidador( PriceList model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PriceList>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo lista de preço duplicado com o mesmo nome. PRL-U5")
		void givenDuplicatePriceList_whenUpdate_thenReturnError() throws Exception {
			PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
			PriceList duplicateModel = new PriceList(2, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceList() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Já existe uma lista de preço com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um lista de preço existente e não pode dar erro de duplicado. AllPartner=false")
		void givenSelfPriceListAllPartnerFalse_whenUpdate_thenNoError() throws Exception {
			PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);			

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceList() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mock ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( mock, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um lista de preço existente e não pode dar erro de duplicado. AllPartner=true")
		void givenSelfPriceListAllPartnerTrue_whenUpdate_thenNoError() throws Exception {
			PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, true);			
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceList() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mock ) ); // Para o método de validation
			when( dao.listOverlay( any() ) ).thenReturn( Arrays.asList( mock ) );
			
			assertDoesNotThrow( ()->service.update( mock, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um lista de preço não existente.")
		void givenNoExistPriceList_whenUpdate_thenReturnError_PRLU4() throws Exception {
			PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "A lista de preço a ser atualizada não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete um lista de preço com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PriceList() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um lista de preço com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um lista de preço com que não existe" )
		void givenNoExistedCanal_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "A lista de preço a ser excluída não existe.", e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "prl_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
	    	Arguments.of( new PriceList(null, null, null, null, null, null) ),
	    	Arguments.of( new PriceList(0, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
   			Arguments.of( new PriceList(null, "PriceList 1", null, null, null, null) ),
   			Arguments.of( new PriceList(null, "PriceList 1", null, null, channel, null) ),
   			Arguments.of( new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, channel, false) ),
   			Arguments.of( new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, null, false) ),
   			Arguments.of( new PriceList(null, "PriceList 1", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
   			Arguments.of( new PriceList(null, "PriceList 1", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, false) ),
   			Arguments.of( new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), null, false) ),
   			Arguments.of( new PriceList(null, null, null, null, channel, false) ),
   			Arguments.of( new PriceList(null, null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
   			Arguments.of( new PriceList(null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, channel, false) ),
   			Arguments.of( new PriceList(null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
   			Arguments.of( new PriceList(null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, null, false) ),
   			Arguments.of( new PriceList(null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, false) ),
   			Arguments.of( new PriceList(null, null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, false) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new PriceList(1, null, null, null, null, null) ),
	    		Arguments.of( new PriceList(0, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
	    		Arguments.of( new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
	   			Arguments.of( new PriceList(1, "PriceList 1", null, null, null, false) ),
	   			Arguments.of( new PriceList(1, "PriceList 1", null, null, channel, false) ),
	   			Arguments.of( new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, channel, false) ),
	   			Arguments.of( new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, null, false) ),
	   			Arguments.of( new PriceList(1, "PriceList 1", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
	   			Arguments.of( new PriceList(1, "PriceList 1", null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, false) ),
	   			Arguments.of( new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), null, false) ),
	   			Arguments.of( new PriceList(1, null, null, null, channel, false) ),
	   			Arguments.of( new PriceList(1, null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
	   			Arguments.of( new PriceList(1, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, channel, false) ),
	   			Arguments.of( new PriceList(1, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), channel, false) ),
	   			Arguments.of( new PriceList(1, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, null, false) ),
	   			Arguments.of( new PriceList(1, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, false) ),
	   			Arguments.of( new PriceList(1, null, null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, false) )
	    );
	}
}
