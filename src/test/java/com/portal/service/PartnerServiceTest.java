package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.PartnerDAO;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.PartnerBrandService;
import com.portal.service.imp.PartnerPersonService;
import com.portal.service.imp.PartnerService;
import com.portal.service.imp.PersonService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PartnerServiceTest {

	@Mock
	PartnerDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	PersonService personService;
	
	@Mock
	ChannelService channelService;

	@Mock
	PartnerPersonService partnerPersonService;
	
	@Mock
	PartnerBrandService partnerBrandService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PartnerService service;
	
	private static final Person personMock = Person.builder()
														.id( 1 )
														.name( "Partner 1" )
														.classification( PersonClassification.PJ.getType() )
														.cnpj( "000000000000014" )
														.build();
	
	private static final Channel channelMock = new Channel(1, "Channel 1", true, true, true);
	
	private static Partner partnerMock = new Partner(1, new Classifier(210), channelMock, personMock );
	
	@Nested
	class Save {

		@DisplayName("Salva um novo parceiro inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PartnerServiceTest#invalidEntityDataToSave" )
		void testEntityValidator( Partner partner ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Partner>> violationSet = validator.validate( partner, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
		
		@Test
		@DisplayName("Salva um novo parceiro com sucesso e retorna o ID")
		void givenPartner_whenSave_thenReturnId() throws Exception {
			Partner mockDB = Partner.builder().id(1).build();
			Partner newPartnerMock = new Partner(null, new Classifier(210), channelMock, personMock );
			
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( dao.save( any() ) ).thenReturn( Optional.of( mockDB ) );
			
			Optional<Partner> obj = service.save( newPartnerMock, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), mockDB.getId() );
		}
		
		@Test
		@DisplayName("Dado um parceiro sem documento quando salva deve dar erro")
		void givenPartnerWithoutDocument_whenSave_thenError() throws Exception {
			Person invalidPerson = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.build();
			
			Partner newPartnerMock = new Partner(null, new Classifier(210), Channel.builder().id(null).build(), invalidPerson );

			BusException ex = assertThrows( BusException.class, ()->service.save(newPartnerMock, null));
			assertEquals( ex.getMessage(), "O número de documento do parceiro é obrigatório.");
		}
		
		@Test
		@DisplayName("Dado um parceiro sem nome quando salva deve dar erro")
		void givenPartnerWithoutName_whenSave_thenError() throws Exception {
			Person invalidPerson = Person.builder()
									.id( 1 )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			Partner newPartnerMock = new Partner(null, new Classifier(210), Channel.builder().id(null).build(), invalidPerson );

			BusException ex = assertThrows( BusException.class, ()->service.save(newPartnerMock, null));
			assertEquals( ex.getMessage(), "O nome do parceiro é obrigatório.");
		}
		
		@Test
		@DisplayName("Dado um parceiro sem classificação inválida quando salva deve dar erro")
		void givenPartnerWithoutClassification_whenSave_thenError() throws Exception {
			Person invalidPerson = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.cnpj( "000000000000014" )
									.build();

			Partner newPartnerMock = new Partner(null, new Classifier(210), Channel.builder().id(null).build(), invalidPerson );

			BusException ex = assertThrows( BusException.class, ()->service.save(newPartnerMock, null));
			assertEquals( ex.getMessage(), "A classificação da pessoa que representa o parceiro não pode ficar sem classificação.");
		}
		
		@Test
		@DisplayName("Dado um parceiro com canal inválido quando salva deve dar erro")
		void givenPartnerInvalidChannel_whenSave_thenError() throws Exception {
			Partner newPartnerMock = new Partner(null, new Classifier(210), Channel.builder().id(null).build(), personMock );
			
			when( personService.find( any(), any() ) ).thenReturn( null );

			BusException ex = assertThrows( BusException.class, ()->service.save(newPartnerMock, null));
			assertEquals( ex.getMessage(), "O canal associado ao parceiro é inválido.");
		}
		
		@Test
		@DisplayName("Dado um parceiro com canal não existente quando salva deve dar erro")
		void givenPartnerNonexistentChannel_whenSave_thenError() throws Exception {
			Partner newPartnerMock = new Partner(null, new Classifier(210), channelMock, personMock );
			
			when( channelService.getById( any() ) ).thenReturn( Optional.empty() );
			when( personService.find( any(), any() ) ).thenReturn( null );

			BusException ex = assertThrows( BusException.class, ()->service.save(newPartnerMock, null));
			assertEquals( ex.getMessage(), "O canal associado ao parceiro não existe.");
		}
		
		@Test
		@DisplayName( "Dado um novo parceiro com uma nova pessoa quando salvamos não deve dar erro" )
		void givenNewPartnerNewPerson_whenSave_thenNoError() throws AppException, BusException {
	
			Person personMock = Person.builder()
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( null );
			
			Partner partner = new Partner(null, new Classifier(210), Channel.builder().id(1).build(), personMock );
			
			assertDoesNotThrow(()->service.save(partner, null));
		}
		
		@Test
		@DisplayName( "Dado um novo parceiro com uma pessoa existente quando salvamos não deve dar erro" )
		void givenNewPartnerExistPerson_whenSave_thenNoError() throws AppException, BusException {
			Person personMock = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( Arrays.asList( personMock ) );
			
			Partner partner = new Partner(null, new Classifier(210), Channel.builder().id(1).build(), personMock );
			
			assertDoesNotThrow(()->service.save(partner, null));
		}
		
		@Test
		@DisplayName( "Dado um novo parceiro com uma pessoa duplicada quando salvamos deve dar erro" )
		void givenNewPartnerDuplicatePerson_whenSave_thenError() throws AppException, BusException {
			
			Person duplicatePersonMock = Person.builder()
											.id( 2 )
											.name( "Partner 2" )
											.classification( PersonClassification.PJ.getType() )
											.cnpj( "000000000000014" )
											.build();
			
			Person personMock = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( Arrays.asList( duplicatePersonMock ) );
			
			Partner partner = new Partner(null, new Classifier(210), Channel.builder().id(1).build(), personMock );
			
			BusException ex = assertThrows( BusException.class, ()->service.save(partner, null));
			assertEquals( ex.getMessage(), "Já existe um parceiro com esse número de documento.");
		}
	}
	
	@Nested
	class Update {
		
		@DisplayName("Atualiza um parceiro com dados inválido e retorna erro.")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PartnerServiceTest#invalidEntityDataToUpdate" )
		void testEntityValidator( Partner partner ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Partner>> violationSet = validator.validate( partner, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
		
		@Test
		@DisplayName("Atualiza um parceiro com sucesso e retorna o ID")
		void givenPartner_whenUpdate_thenReturnId() throws Exception {
			Partner mockDB = Partner.builder().id(1).build();
			Partner partner = new Partner(1, new Classifier(210), channelMock, personMock );
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( partner ) );
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( null );
			when( dao.update( any() ) ).thenReturn( Optional.of( mockDB ) );
			
			Optional<Partner> obj = service.update( partner, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), mockDB.getId() );
		}
		
		@Test
		@DisplayName("Dado um parceiro sem documento quando atualiza deve dar erro")
		void givenPartnerWithoutDocument_whenUpdate_thenError() throws Exception {
			Person invalidPerson = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.build();
			
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(null).build(), invalidPerson );

			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(partner, null));
			assertEquals( ex.getMessage(), "O número de documento do parceiro é obrigatório.");
		}
		
		@Test
		@DisplayName("Dado um parceiro sem nome quando atualiza deve dar erro")
		void givenPartnerWithoutName_whenUpdate_thenError() throws Exception {
			Person invalidPerson = Person.builder()
									.id( 1 )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(null).build(), invalidPerson );

			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(partner, null));
			assertEquals( ex.getMessage(), "O nome do parceiro é obrigatório.");
		}
		
		@Test
		@DisplayName("Dado um parceiro sem classificação inválida quando atualiza deve dar erro")
		void givenPartnerWithoutClassification_whenUpdate_thenError() throws Exception {
			Person invalidPerson = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.cnpj( "000000000000014" )
									.build();
			
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(null).build(), invalidPerson );

			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(partner, null));
			assertEquals( ex.getMessage(), "A classificação da pessoa que representa o parceiro não pode ficar sem classificação.");
		}
		
		@Test
		@DisplayName("Dado um parceiro com canal inválido quando atualiza deve dar erro")
		void givenPartnerInvalidChannel_whenUpdate_thenError() throws Exception {
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(null).build(), personMock );
			
			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			when( personService.find( any(), any() ) ).thenReturn( null );

			BusException ex = assertThrows( BusException.class, ()->service.update(partner, null));
			assertEquals( ex.getMessage(), "O canal associado ao parceiro é inválido.");
		}
		
		@Test
		@DisplayName("Dado um parceiro com canal não existente quando atualiza deve dar erro")
		void givenPartnerNonexistentChannel_whenUpdate_thenError() throws Exception {
			Partner partner = new Partner(1, new Classifier(210), channelMock, personMock );
			
			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			when( channelService.getById( any() ) ).thenReturn( Optional.empty() );
			when( personService.find( any(), any() ) ).thenReturn( null );

			BusException ex = assertThrows( BusException.class, ()->service.update(partner, null));
			assertEquals( ex.getMessage(), "O canal associado ao parceiro não existe.");
		}
		
		@Test
		@DisplayName( "Dado um parceiro com uma nova pessoa quando atualizamos não deve dar erro" )
		void givenPartnerNewPerson_whenUpdate_thenNoError() throws AppException, BusException {
	
			Person personMock = Person.builder()
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(1).build(), personMock );
			
			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( null );
			
			assertDoesNotThrow(()->service.update(partner, null));
		}
		
		@Test
		@DisplayName( "Dado um parceiro com uma pessoa existente quando atualizamos não deve dar erro" )
		void givenPartnerExistPerson_whenUpdate_thenNoError() throws AppException, BusException {
			Person personMock = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(1).build(), personMock );
			
			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( Arrays.asList( personMock ) );
			
			assertDoesNotThrow(()->service.update(partner, null));
		}
		
		@Test
		@DisplayName( "Dado um parceiro com uma pessoa duplicada quando atualizamos deve dar erro" )
		void givenPartnerDuplicatePerson_whenUpdate_thenError() throws AppException, BusException {
			
			Person duplicatePersonMock = Person.builder()
											.id( 2 )
											.name( "Partner 2" )
											.classification( PersonClassification.PJ.getType() )
											.cnpj( "000000000000014" )
											.build();
			
			Person personMock = Person.builder()
									.id( 1 )
									.name( "Partner 1" )
									.classification( PersonClassification.PJ.getType() )
									.cnpj( "000000000000014" )
									.build();
			
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(1).build(), personMock );

			when( dao.getById( any() ) ).thenReturn( Optional.of(partner) );
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( Arrays.asList( duplicatePersonMock ) );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(partner, null));
			assertEquals( ex.getMessage(), "Já existe um parceiro com esse número de documento.");
		}
		
		@Test
		@DisplayName( "Dado um parceiro que não existe quando atualizamos deve dar erro" )
		void givenNonexistentPartner_whenUpdate_thenError() throws AppException, BusException {
			
			Partner partner = new Partner(1, new Classifier(210), Channel.builder().id(1).build(), personMock );

			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			when( channelService.getById( any() ) ).thenReturn( Optional.of( channelMock ) );
			when( personService.find( any(), any() ) ).thenReturn( Arrays.asList() );
			
			BusException ex = assertThrows( BusException.class, ()->service.update(partner, null));
			assertEquals( ex.getMessage(), "O parceiro a ser atualizado não existe." );
		}
		
	}
	
	@Nested
	class Get {
		
		@Test
		@DisplayName("Dado um ID de parceiro válido quando busca por ID retorna a entidade")
		void givenPartnerID_whenGetById_thenReturnId() throws Exception {
			Partner partner = new Partner(1, new Classifier(210), channelMock, personMock );
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( partner ) );
			
			Optional<Partner> obj = service.getById( partner.getId() );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), partner.getId() );
		}
		
		@Test
		@DisplayName("Dado um ID de parceiro inválido quando busca por ID retorna erro")
		void givenInvalidPartnerID_whenGetById_thenReturnId() throws Exception {
			BusException ex = assertThrows( BusException.class, ()->service.getById( null ));
			assertEquals( ex.getMessage(), "ID de busca inválido." );
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um parceiro com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( partnerMock ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um parceiro com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um parceiro com que não existe" )
		void givenNoExistedPartner_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O parceiro a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um parceiro com relacionamento com lista de preço e retorna erro" )
		void givenGroupInPartnerRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new Partner() ) );
			when( dao.hasPriceListRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o parceiro pois existe um relacionamento com lista de preço.", e.getMessage());
		}
	}
	
	@Nested
	class Sync {
		@Test
		@DisplayName( "Dado novas pessoas do parceiro devemos adicionar" )
		void givenNewEmploees_whenSyncPartnerPerson_thenNoError() throws Exception {
			
		}
		
		@Test
		@DisplayName( "Dado novas pessoas do parceiro devemos adicionar" )
		void givenNewBrands_whenSyncPartnerPerson_thenNoError() throws Exception {
			
			List<Brand> newBrands = Arrays.asList(
					new Brand( 1, "Brand 1", true ),
					new Brand( 2, "Brand 2", true ),
					new Brand( 3, "Brand 3", true )
				);
			
			Partner partner = new Partner(1, new Classifier(210), channelMock, personMock);
			partner.setBrandList( newBrands );
			
			when( partnerBrandService.findByPartner(any()) ).thenReturn( null );
			
			Method m = PartnerService.class.getDeclaredMethod("syncPartnerBrandRelationship", Partner.class);
			m.setAccessible( true );
			assertDoesNotThrow(()->m.invoke( service, partner ));
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSave() {
	    return Stream.of(
			Arguments.of( new Partner(null, new Classifier(210), null, null) ),
			Arguments.of( new Partner(0, new Classifier(210), channelMock, personMock) ),
			Arguments.of( new Partner(null, new Classifier(210), null, null) ),
			Arguments.of( new Partner(null, new Classifier(210), channelMock, null) ),
			Arguments.of( new Partner(null, new Classifier(210), null, personMock) ),
			Arguments.of( new Partner(null, null, channelMock, null) ),
			Arguments.of( new Partner(null, null, channelMock, personMock) ),
			Arguments.of( new Partner(null, null, null, personMock) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdate() {
	    return Stream.of(
			Arguments.of( new Partner(1, new Classifier(210), null, null) ),
			Arguments.of( new Partner(0, new Classifier(210), channelMock, personMock) ),
			Arguments.of( new Partner(null, new Classifier(210), channelMock, personMock) ),
			Arguments.of( new Partner(1, new Classifier(210), null, null) ),
			Arguments.of( new Partner(1, new Classifier(210), channelMock, null) ),
			Arguments.of( new Partner(1, new Classifier(210), null, personMock) ),
			Arguments.of( new Partner(1, null, channelMock, null) ),
			Arguments.of( new Partner(1, null, channelMock, personMock) ),
			Arguments.of( new Partner(1, null, null, personMock) )
	    );
	}
}
