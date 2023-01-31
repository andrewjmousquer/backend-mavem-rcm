package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.mockito.Spy;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.LeadDAO;
import com.portal.enums.LeadState;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.enums.PersonClassification;
import com.portal.enums.SaleProbabilty;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Lead;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.model.Seller;
import com.portal.model.Source;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.LeadService;
import com.portal.utils.LeadStateBuilder;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class LeadServiceTest {

	@Mock
	LeadDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@Spy
	LeadStateBuilder buyilder;
	
	@Spy
	LeadService serviceInternal;

	@InjectMocks
	LeadService service;
	
	private static final Person clientMock = new Person( 1, "Person Client", "Client", "00000000001", null, null, null, null, PersonClassification.PF.getType() );
	private static final Seller sellerMock = Seller.builder().person( new Person( 2, "Person Seller", "Seller", "00000000002", null, null, null, null, PersonClassification.PF.getType() )).build();
	private static final Source sourceMock = new Source( 1, "Source 1", true );
	private static final Brand brandMock = new Brand( 1, "Brand 1", true );
	private static final Model modelMock = new Model( 1, "Model 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	
	@Nested
	class ListAll {
		@DisplayName("Listar os leads e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.LeadServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnLeadList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Lead() ) );

			List<Lead> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva uma novo lead válido e retorna a marca com ID")
		void givenValidLead_whenSave_thenReturnId() throws Exception {
			
			Lead mock = Lead.builder().id(null).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
					.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType())
					.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 1").subject("").build();

					//new Lead(null, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, "");

			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Lead> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getCreateDate(), entityDB.get().getCreateDate() );			
			assertEquals( mock.getDescription(), entityDB.get().getDescription() );
			assertNotNull( entityDB.get().getClient() );
			assertEquals( mock.getClient(), entityDB.get().getClient() );
			assertNotNull( entityDB.get().getSeller() );
			assertEquals( mock.getSeller(), entityDB.get().getSeller() );
			assertNotNull( entityDB.get().getSource() );
			assertEquals( mock.getSource(), entityDB.get().getSource() );
			assertNotNull( entityDB.get().getStatus() );
			assertEquals( mock.getStatus(), entityDB.get().getStatus() );
			assertNotNull( entityDB.get().getModel() );
			assertEquals( mock.getModel(), entityDB.get().getModel() );			
		}
		
		@DisplayName("Salva um lead e da erro nos validators")
		@ParameterizedTest
		@MethodSource( "com.portal.service.LeadServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidLead_whenSave_thenTestValidador( Lead model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Lead>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza uma lead válido e retorna com a atualização")
		void givenLead_whenUpdate_thenReturnNewLead() throws Exception {

			Lead mock = Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
					.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType())
					.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 1").subject("").build();			
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( mock ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Lead> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getCreateDate(), entityDB.get().getCreateDate() );			
			assertEquals( mock.getDescription(), entityDB.get().getDescription() );
			assertNotNull( entityDB.get().getClient() );
			assertEquals( mock.getClient(), entityDB.get().getClient() );
			assertNotNull( entityDB.get().getSeller() );
			assertEquals( mock.getSeller(), entityDB.get().getSeller() );
			assertNotNull( entityDB.get().getSource() );
			assertEquals( mock.getSource(), entityDB.get().getSource() );
			assertNotNull( entityDB.get().getStatus() );
			assertEquals( mock.getStatus(), entityDB.get().getStatus() );
			assertNotNull( entityDB.get().getModel() );
			assertEquals( mock.getModel(), entityDB.get().getModel() );
		}
		
		@DisplayName("Atualiza uma lead inválido e retorna erro")
		@ParameterizedTest
		@MethodSource( "com.portal.service.LeadServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidLead_whenUpdate_thenTestValidador( Lead model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Lead>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza uma lead não existente")
		void givenNoExistLead_whenUpdate_thenReturnError_CHNU4() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );

			Lead mock = Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
					.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType())
					.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 1").subject("").build(); 
					
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "O lead a ser atualizado não existe.", e.getMessage());
		}
	
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Delete um lead com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Lead() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Deleta uma lead com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Deleta uma lead com que não existe" )
		void givenNoExistedCanal_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "A lead a ser excluída não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Deleta uma lead com relacionamento com proposta e retorna erro" )
		void givenLeadInLeadRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( new Lead() ) );
			when( dao.hasProposalRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir a fonte pois existe um relacionamento com lead.", e.getMessage());
		}
	}
	
	@Nested
	class Find {
		
		@ParameterizedTest
		@MethodSource("com.portal.service.LeadServiceTest#listEntityToFind")
		@DisplayName( "Quando busca retornar os IDs salvos" )
		void whenFind_ReturnLead( Lead mock, Lead filter, int expectedSize ) throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mock ) );
			
			List<Lead> entityDB = dao.find( filter, null );
			
			assertNotNull( entityDB );
			assertFalse( entityDB.isEmpty() );
			assertEquals( expectedSize, entityDB.size() );
			
			Optional<Lead> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
			
			assertNotNull( entity );
			assertTrue( entity.isPresent() );
			assertEquals( mock, entity.get() );
			assertEquals( mock.getCreateDate(), entity.get().getCreateDate() );
			assertEquals( mock.getDescription(), entity.get().getDescription() );
			assertNotNull( entity.get().getClient() );
			assertEquals( mock.getClient(), entity.get().getClient() );
			assertNotNull( entity.get().getSeller() );
			assertEquals( mock.getSeller(), entity.get().getSeller() );
			assertNotNull( entity.get().getSource() );
			assertEquals( mock.getSource(), entity.get().getSource() );
			assertNotNull( entity.get().getStatus() );
			assertEquals( mock.getStatus(), entity.get().getStatus() );
			assertNotNull( entity.get().getModel() );
			assertEquals( mock.getModel(), entity.get().getModel() );
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "led_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
		modelMock.setBrand(brandMock);		
	    return Stream.of(
	    		Arguments.of( Lead.builder().build() ),	    		
	    		Arguments.of( Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()) .build() ),
	    		Arguments.of( Lead.builder().id(0).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).build() ),
	    		Arguments.of( Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).build() ),
	    		Arguments.of( Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").build() ),
	    		Arguments.of( Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).build() ),
	    		Arguments.of( Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).build() ),
	    		Arguments.of( Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).build() ),
	    		Arguments.of( Lead.builder().description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().status(LeadState.OPENED.getType()).build() )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
		modelMock.setBrand(brandMock);
		return Stream.of(
	    		Arguments.of( Lead.builder().id(1).build() ),	    		
	    		Arguments.of( Lead.builder().id(0).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build() ),
	    		Arguments.of( Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build() ),
	    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).build() ),
	    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).build() ),
	    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").build() ),
	    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).build() ),
	    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).build() ),
	    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).build() ),
	    		Arguments.of( Lead.builder().id(1).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().id(1).client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().id(1).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().id(1).source(sourceMock).status(LeadState.OPENED.getType()).build() ),
	    		Arguments.of( Lead.builder().id(1).status(LeadState.OPENED.getType()).build() )
	    		);
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
	    return Stream.of(
    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build(), Lead.builder().id( 1 ).build(), 1 ),
    		Arguments.of( Lead.builder().id(0).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build(), Lead.builder().createDate(LocalDateTime.of(2020, 12, 10, 00, 00, 00, 00)).build(), 1 ),
    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build(), Lead.builder().client(clientMock).build(), 1 ),
    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build(), Lead.builder().seller(sellerMock).build(), 1 ),
    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build(), Lead.builder().source(sourceMock).build(), 1 ),
    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build(), Lead.builder().status( LeadState.OPENED.getType() ).build(), 1 ),
    		Arguments.of( Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).description("NOTES 1").client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType()).model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).build(), Lead.builder().model(modelMock).build(), 1 )
	    );
	}
}


