package com.portal.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.portal.dao.impl.PartnerBrandDAO;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PartnerBrandService;
import com.portal.service.imp.PartnerService;

@ExtendWith(SpringExtension.class)	
public class PartnerBrandServiceTest {

	@Mock
	PartnerBrandDAO dao;

	@Mock
	BrandService brandService;
	
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
	PartnerBrandService service;
	
	@Nested
	class Find {
		@Test
		@DisplayName( "Dado o ID válido de um parceiro quando procurado retorna a lista de marcas" )
		void givenValidPartnerId_whenFindByPartner_thenReturnBrandList() throws Exception { 
			when( dao.findByPartner( any() ) ).thenReturn( Arrays.asList( new Brand() ) );
			
			List<Brand> list = service.findByPartner( 1 );
			
			assertNotNull(list);
			assertFalse(list.isEmpty());
			assertEquals(1, list.size());
		}
		
		@Test
		@DisplayName( "Dado o ID NULO de um parceiro quando procurado retorna erro" )
		void givenNullPartnerId_whenFindByPartner_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( null ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero de um parceiro quando procurado retorna erro" )
		void givenZeroPartnerId_whenFindByPartner_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro inválido." );
		}
		
		void givenValidBrandId_whenFindByBrand_thenReturnPartner() throws Exception { 
			when( dao.findByBrand( any() ) ).thenReturn( Arrays.asList( new Partner() ) );
			
			List<Partner> list = service.findByBrand( 1 );
			
			assertNotNull(list);
			assertFalse(list.isEmpty());
			assertEquals(1, list.size());
		}
		
		@Test
		@DisplayName( "Dado o ID NULO de um parceiro quando procurado retorna erro" )
		void givenNullBrandId_whenFindByBrand_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByBrand( null ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero de um parceiro quando procurado retorna erro" )
		void givenZeroBrandId_whenFindByBrand_thenReturnError() throws Exception { 
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro inválido." );
		}
	}
	
	@Nested
	class Get {
		@Test
		@DisplayName( "Dado o ID válido de um parceiro e uma marca quando procurado retorna a marcas relacionada" )
		void givenValidPartnerBrandId_whenGetBrand_thenReturnBrand() throws Exception {
			Brand mock = new Brand( 1, "Brand 1", true );
			
			when( brandService.find( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Brand> brand = brandService.getById( 1 );
			
			assertNotNull( brand );
			assertTrue( brand.isPresent() );
			assertEquals( mock, brand.get());
			assertEquals( mock.getName(), brand.get().getName());
			assertEquals( mock.getActive(), brand.get().getActive());
		}
		
		@Test
		@DisplayName( "Dado o ID zero do parceiro quando busca por uma marca deve dar erro" )
		void givenZeroPartnerId_whenGetBrand_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo do parceiro quando busca por uma marca deve dar erro" )
		void givenNullPartnerId_whenGetBrand_thenReturnTypeList() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByBrand( 1 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero da marca quando busca por uma marca deve dar erro" )
		void givenZeroBrandId_whenGetBrand_thenReturnTypeList() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo da marca quando busca por uma marca deve dar erro" )
		void givenNullBrandId_whenGetBrand_thenReturnTypeList() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByPartner( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID válido de uma marca e um parceirto quando procurado retorna a marca relacionada" )
		void givenValidPartnerBrandId_whenGetPartner_thenReturnBrand() throws Exception {
			Channel channel = new Channel(1, "Channel 1", true, true, true);
			Person person = new Person( null, "Person PF", "Vendedor", "00000000001", null, null, null, null, PersonClassification.PF.getType() );
			Partner mock = new Partner(1, new Classifier(210), channel, person, null);
			
			when( partnerService.find( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Partner> partner = partnerService.getById( 0 );
			
			assertNotNull( partner );
			assertTrue( partner.isPresent() );
			assertEquals( mock, partner.get());
			assertEquals( mock.getChannel(), partner.get().getChannel());
			assertEquals( mock.getPartnerGroup(), partner.get().getPartnerGroup());
			assertEquals( mock.getPerson(), partner.get().getPerson());
			assertEquals( mock.getSituation(), partner.get().getSituation());
		}
		
		@Test
		@DisplayName( "Dado o ID zero do parceiro quando busca por uma marca deve dar erro" )
		void givenZeroPartnerId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByBrand( 0 ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo do parceiro quando busca por uma marca deve dar erro" )
		void givenNullPartnerId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByBrand( null )  );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID zero da marca quando busca por uma marca deve dar erro" )
		void givenZeroBrandId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByBrand( 0 )  );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
		
		@Test
		@DisplayName( "Dado o ID nulo da marca quando busca por uma marca deve dar erro" )
		void givenNullBrandId_whenGetPartner_thenReturnError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.findByBrand( null ) );
			assertEquals( ex.getMessage(), "Não é possível buscar o relacionamento entre marca e parceiro com o ID do parceiro ou da marca inválido." );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Dado um relacionamento quando salvo não deve dar erro")
		void givenPartnerBrandRelatioship_whenSave_thenReturnId() throws Exception {
			when( partnerService.getById( any() ) ).thenReturn( Optional.of( Partner.builder().id(1).build() ) );
			when( brandService.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			
			assertDoesNotThrow(()->service.save( 1, 1, null ) );
		}
		
		@Test
		@DisplayName("Dado uma marca inexistente quando salvo deve ocorre erro")
		void givenNoExistBrand_whenSave_thenReturnError() throws Exception {
			when( brandService.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException ex = assertThrows( BusException.class, ()->service.save( 1, 1, null ) );
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento entre marca e parceiro com a marca inválida ou inexistente." );
		}
		
		@Test
		@DisplayName("Dado um parceiro inexistente quando salvo deve ocorre erro")
		void givenNoExistType_whenSave_thenReturnError() throws Exception {
			when( partnerService.getById( any() ) ).thenReturn( Optional.empty() );
			when( brandService.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			
			BusException ex = assertThrows( BusException.class, ()->service.save( 1, 1, null ) );
			assertEquals( ex.getMessage(), "Não é possível salvar o relacionamento entre marca e parceiro com o parceiro inválido ou inexistente." );
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Dado um ID de parceiro válido quando deleta então não deve dar erro" )
		void givenValidPartnerId_whenDeleteByPartner_thenNoError() throws Exception {
			assertDoesNotThrow( ()->service.delete(1, null, null) );
		}
		
		@Test
		@DisplayName( "Dado um ID NULL de parceiro quando deleta então deve dar erro" )
		void givenNullBrandId_whenDeleteByPartner_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( null, null, null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre marca e parceiro com o ID do parceiro inválido." );
		}
		
		@Test
		@DisplayName( "Dado um ID zero (0) de parceiro quando deleta então deve dar erro" )
		void givenZeroBrandId_whenDeleteByBrand_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( 0, null, null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre marca e parceiro com o ID do parceiro inválido." );
		}
		
		@Test
		@DisplayName( "Dado IDs de marca e parceiro válidos quando deleta então não deve dar erro" )
		void givenValidIds_whenDelete_thenNoError() throws Exception {
			assertDoesNotThrow( ()->service.delete(1,1, null) );
		}
		
		@Test
		@DisplayName( "Dado um ID NULL de marca quando deleta então deve dar erro" )
		void givenNullBrandId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( null, 1, null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre marca e parceiro com o ID da marca e/ou parceiro inválidos." );
		}
		
		@Test
		@DisplayName( "Dado um ID zero (0) de marca quando deleta então deve dar erro" )
		void givenZeroBrandId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( 0, 1, null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre marca e parceiro com o ID da marca e/ou parceiro inválidos." );
		}
		
		@Test
		@DisplayName( "Dado um ID NULL de parceiro quando deleta então deve dar erro" )
		void givenNullTypeId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( 1, null, null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre marca e parceiro com o ID da marca e/ou parceiro inválidos." );
		}
		
		@Test
		@DisplayName( "Dado um ID zero (0) de parceiro quando deleta então deve dar erro" )
		void givenZeroTypeId_whenDelete_thenError() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.delete( 1, 0, null ) );
			assertEquals( ex.getMessage(), "Não é possível excluir o relacionamento entre marca e parceiro com o ID da marca e/ou parceiro inválidos." );
		}
	}
}
