package com.portal.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.PriceListPartnerDAO;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.model.PriceList;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PartnerService;
import com.portal.service.imp.PriceListPartnerService;
import com.portal.service.imp.PriceListService;

@ExtendWith(SpringExtension.class)	
public class PriceListPartnerServiceTest {

	@Mock
	PriceListPartnerDAO dao;

	@Mock
	PriceListService priceListService;
	
	@Mock
	PartnerService partnerService;
	
	@Mock
	ClassifierService classifierService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PriceListPartnerService service;
	
	@Nested
	class Find {
		@Test
		@DisplayName( "Dado o ID válido de um parceiro quando procurado retorna a lista de preços" )
		void givenValidPartnerId_whenFindByPartner_thenReturnPriceListList() throws Exception { 
			when( dao.findByPartner( any() ) ).thenReturn( Arrays.asList( new PriceList() ) );
			
			List<PriceList> list = service.findByPartner( 1 );
			
			assertNotNull(list);
			assertFalse(list.isEmpty());
			assertEquals(1, list.size());
		}
		
		@Test
		@DisplayName( "Dado o ID NULO de um parceiro quando procurado retorna erro" )
		void givenNullPartnerId_whenFindByPartner_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( null ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero de um parceiro quando procurado retorna erro" )
		void givenZeroPartnerId_whenFindByPartner_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID válido de uma lista de preço quando procurado retorna a lista de parceiros" )
		void givenValidPriceListId_whenFindByPriceList_thenReturnPartner() throws Exception { 
			when( dao.findByPriceList( any() ) ).thenReturn( Arrays.asList( new Partner() ) );
			
			List<Partner> list = service.findByPriceList( 1 );
			
			assertNotNull(list);
			assertFalse(list.isEmpty());
			assertEquals(1, list.size());
		}
		
		@Test
		@DisplayName( "Dado o ID NULO de uma lista de preço quando procurado retorna erro" )
		void givenNullPriceListId_whenFindByPriceList_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByPriceList( null ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero de uma lista de preço quando procurado retorna erro" )
		void givenZeroPriceListId_whenFindByPriceList_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByPriceList( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID da lista e preço inválido." );
		}
	}
	
	@Nested
	class Get {
		@Test
		@DisplayName( "Dado o ID válido de um parceiro e uma lista de preço quando procurado retorna a lista de preços relacionada" )
		void givenValidPartnerPriceListId_whenGetPriceList_thenReturnPriceList() throws Exception {
			
			Channel channel = new Channel(1, "Channel 1", true, true, true);
			PriceList mock = new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
			
			when( dao.getPriceList( any(), any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<PriceList> entityDB = service.getPriceList( 1, 1 );
			
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
		
		@Test
		@DisplayName( "Dado o ID zero do parceiro quando busca por uma lista de preço deve dar erro" )
		void givenZeroPartnerId_whenGetPriceList_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPriceList( 1, 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo do parceiro quando busca por uma lista de preço deve dar erro" )
		void givenNullPartnerId_whenGetPriceList_thenReturnTypeList() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPriceList( 1, null ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero da lista de preço quando busca por uma lista de preço deve dar erro" )
		void givenZeroPriceListId_whenGetPriceList_thenReturnTypeList() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPriceList( 0, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo da lista de preço quando busca por uma lista de preço deve dar erro" )
		void givenNullPriceListId_whenGetPriceList_thenReturnTypeList() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPriceList( null, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID válido de uma lista de preço e um parceirto quando procurado retorna a lista de preço relacionada" )
		void givenValidPartnerPriceListId_whenGetPartner_thenReturnPriceList() throws Exception {
			Channel channel = new Channel(1, "Channel 1", true, true, true);
			Person person = new Person( null, "Person PF", "Vendedor", "00000000001", null, null, null, null, PersonClassification.PF.getType() );
			Partner mock = new Partner(1, new Classifier(210), channel, person, null);
			
			when( dao.getPartner( any(), any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Partner> partner = service.getPartner( 1, 1 );
			
			assertNotNull( partner );
			assertTrue( partner.isPresent() );
			assertEquals( mock, partner.get());
			assertEquals( mock.getChannel(), partner.get().getChannel());
			assertEquals( mock.getPartnerGroup(), partner.get().getPartnerGroup());
			assertEquals( mock.getPerson(), partner.get().getPerson());
			assertEquals( mock.getSituation(), partner.get().getSituation());
		}
		
		@Test
		@DisplayName( "Dado o ID zero do parceiro quando busca por uma lista de preço deve dar erro" )
		void givenZeroPartnerId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPartner( 1, 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo do parceiro quando busca por uma lista de preço deve dar erro" )
		void givenNullPartnerId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPartner( 1, null ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero da lista de preço quando busca por uma lista de preço deve dar erro" )
		void givenZeroPriceListId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPartner( 0, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo da lista de preço quando busca por uma lista de preço deve dar erro" )
		void givenNullPriceListId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getPartner( null, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre lista e preço e parceiro com o ID do parceiro ou da lista e preço inválido." );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Dado um relacionamento quando salvo não deve dar erro")
		void givenPartnerPriceListRelatioship_whenSave_thenReturnId() throws Exception {
			when( partnerService.getById( any() ) ).thenReturn( Optional.of( Partner.builder().id(1).build() ) );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( PriceList.builder().id(1).build() ) );
			
			assertDoesNotThrow(()->service.save( 1, 1 ) );
		}
		
		@Test
		@DisplayName("Dado uma lista de preço inexistente quando salvo deve ocorre erro")
		void givenNoExistPriceList_whenSave_thenReturnError() throws Exception {
			when( priceListService.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException ex = assertThrows( BusException.class, ()->service.save( 1, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento entre lista e preço e parceiro com a lista e preço inválida ou inexistente." );
		}
		
		@Test
		@DisplayName("Dado um parceiro inexistente quando salvo deve ocorre erro")
		void givenNoExistType_whenSave_thenReturnError() throws Exception {
			when( partnerService.getById( any() ) ).thenReturn( Optional.empty() );
			when( priceListService.getById( any() ) ).thenReturn( Optional.of( PriceList.builder().id(1).build() ) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save( 1, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento entre lista e preço e parceiro com o parceiro inválido ou inexistente." );
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Dado um ID de uma lista de preço válido quando deleta então não deve dar erro" )
		void givenValidPartnerId_whenDeleteByPartner_thenNoError() throws Exception {
			assertDoesNotThrow( ()->service.deleteByPriceList(1) );
		}
		
		@Test
		@DisplayName( "Dado um ID NULL de lista de preço quando deleta então deve dar erro" )
		void givenNullPriceListId_whenDeleteByPartner_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.deleteByPriceList( null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista de preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado um ID zero (0) de parceiro quando deleta então deve dar erro" )
		void givenZeroPriceListId_whenDeleteByPriceList_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.deleteByPriceList( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista de preço inválido." );
		}
		
		@Test
		@DisplayName( "Dado IDs de lista de preço e parceiro válidos quando deleta então não deve dar erro" )
		void givenValidIds_whenDelete_thenNoError() throws Exception {
			assertDoesNotThrow( ()->service.delete(1,1) );
		}
		
		@Test
		@DisplayName( "Dado um ID NULL de lista de preço quando deleta então deve dar erro" )
		void givenNullPriceListId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( null, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista e preço e/ou parceiro inválidos." );
		}
		
		@Test
		@DisplayName( "Dado um ID zero (0) de lista de preço quando deleta então deve dar erro" )
		void givenZeroPriceListId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( 0, 1 ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista e preço e/ou parceiro inválidos." );
		}
		
		@Test
		@DisplayName( "Dado um ID NULL de parceiro quando deleta então deve dar erro" )
		void givenNullTypeId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista e preço e/ou parceiro inválidos." );
		}
		
		@Test
		@DisplayName( "Dado um ID zero (0) de parceiro quando deleta então deve dar erro" )
		void givenZeroTypeId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( 1, 0 ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre lista e preço e parceiro com o ID da lista e preço e/ou parceiro inválidos." );
		}
	}
}
