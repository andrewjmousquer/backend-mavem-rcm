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

import java.time.LocalDate;
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
import com.portal.dao.impl.VehicleDAO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.VehicleModel;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ModelService;
import com.portal.service.imp.VehicleService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class VehicleServiceTest {

	@Mock
	VehicleDAO dao;
	
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
	
	@Spy
	VehicleService serviceInternal;

	@InjectMocks
	VehicleService service;
	
	private static Brand brandMock = new Brand( 1, "BRAND 1", true );
	private static Model modelMock = new Model( 1, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	
	@Nested
	class ListAll {
		@DisplayName("Listar o veículo e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.VehicleServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnProposalList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new VehicleModel() ) );

			List<VehicleModel> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "vhe_id") );
			assertFalse( list.isEmpty() );
		}
		
		@DisplayName("Dada a paginação nula então retorna a lista")
		@Test
		void givenNullPagination_whenListAll_thenReturnProposalList() throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new VehicleModel() ) );

			List<VehicleModel> list = service.listAll( null );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Dado um veículo válido quando salvar retornar o objeto com o novo ID")
		void givenValidVehicle_whenSave_thenReturnId() throws Exception {

			VehicleModel model = new VehicleModel(1, "111111111111112", "aaa12345", modelMock, null,2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);
			VehicleModel mock = new VehicleModel(1, "111111111111112", "aaa12345", modelMock, null,2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);

			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( modelService.getById( anyInt() ) ).thenReturn( Optional.of( modelMock ) );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<VehicleModel> entityDB = service.save( model, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getId(), entityDB.get().getId() );
			assertEquals( mock.getChassi(), entityDB.get().getChassi() );
			assertEquals( mock.getPlate(), entityDB.get().getPlate() );
			assertNotNull( entityDB.get().getModel() );
			assertEquals( mock.getModel(), entityDB.get().getModel() );
			assertEquals( mock.getModelYear(), entityDB.get().getModelYear() );
			assertEquals( mock.getPurchaseDate(), entityDB.get().getPurchaseDate() );
			assertEquals( mock.getPurchaseValue(), entityDB.get().getPurchaseValue() );
		}
		
		@Test
		@DisplayName("Dada uma proposta duplicada quando salvar deve dar erro")
		void givenDuplicateVehicle_whenSave_thenReturnError() throws Exception {

			VehicleModel mockDuplicate = new VehicleModel(2, "111111111111112", "aaa12345", modelMock, null,2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);

			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mockDuplicate ) );
			
			BusException e = assertThrows( BusException.class, ()->service.save( new VehicleModel(), null ) );
			assertEquals( "Já existe um veículo com a mesma placa.", e.getMessage() );
		}
		
		@Test
		@DisplayName("Dada um registro com o modelo não existente quando salvar deve dar erro")
		void givenVehicleWithInvalidModel_whenSave_thenReturnError() throws Exception {

			VehicleModel mock = new VehicleModel(1, "111111111111112", "aaa12345", modelMock, null,2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);

			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( modelService.getById( anyInt() ) ).thenReturn( Optional.empty() );
			
			BusException e = assertThrows( BusException.class, ()->service.save( mock, null ) );
			assertEquals( "Não é possível salvar o veículo da proposta com o modelo não existente.", e.getMessage());
		}
		
		@DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
		@ParameterizedTest
		@MethodSource( "com.portal.service.VehicleServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidVehicle_whenSave_thenTestValidador( VehicleModel model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<VehicleModel>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Dada um detalhe de veículo da proposta válido quando atualiza retornar o objeto com o novo ID")
		void givenValidVehicle_whenUpdate_thenReturnId() throws Exception {

			VehicleModel model = new VehicleModel(1, "111111111111112", "aaa12345", modelMock, null, 2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);
			VehicleModel mock = new VehicleModel(1, "111111111111112", "aaa12345", modelMock, null, 2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);

			
			when( dao.getById( any() ) ).thenReturn( Optional.of(mock) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( modelService.getById( anyInt() ) ).thenReturn( Optional.of( modelMock ) );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<VehicleModel> entityDB = service.update( model, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getId(), entityDB.get().getId() );
			assertEquals( mock.getChassi(), entityDB.get().getChassi() );
			assertEquals( mock.getPlate(), entityDB.get().getPlate() );
			assertNotNull( entityDB.get().getModel() );
			assertEquals( mock.getModel(), entityDB.get().getModel() );
			assertEquals( mock.getModelYear(), entityDB.get().getModelYear() );
			assertEquals( mock.getPurchaseDate(), entityDB.get().getPurchaseDate() );
			assertEquals( mock.getPurchaseValue(), entityDB.get().getPurchaseValue() );
		}
		
		@Test
		@DisplayName("Dada uma proposta duplicada quando salvar deve dar erro")
		void givenDuplicateVehicle_whenUpdate_thenReturnError() throws Exception {

			VehicleModel mockDuplicate = new VehicleModel(2, "111111111111112", "aaa12345", modelMock, null,2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);

			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mockDuplicate ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( new VehicleModel(), null ) );
			assertEquals( "Já existe um veículo com a mesma placa.", e.getMessage() );
		}
		
		@Test
		@DisplayName("Dada um registro com o modelo não existente quando salvar deve dar erro")
		void givenVehicleWithInvalidModel_whenUpdate_thenReturnError() throws Exception {

			VehicleModel mock = new VehicleModel(1, "111111111111112", "aaa12345", modelMock, null,2021,  LocalDate.of(2019, 10, 10), 10000d, null, null);

			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( modelService.getById( anyInt() ) ).thenReturn( Optional.empty() );
			
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "Não é possível salvar o veículo da proposta com o modelo não existente.", e.getMessage());
		}
		
		@DisplayName("Dado dados inválidos da entidade aplicamos as validaçoes e deve retornar erro")
		@ParameterizedTest
		@MethodSource( "com.portal.service.VehicleServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidVehicle_whenUpdate_thenTestValidador( VehicleModel model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<VehicleModel>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
	}
	
	@Nested
	class Delete {
		@Test
		@DisplayName( "Dado um ID válido do veículo quando deleta não da erro" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.hasProposalDetailRelationship( any() ) ).thenReturn( false );
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new VehicleModel() ) );
			assertDoesNotThrow( ()->service.delete( 1, null ) );
		}
		
		@Test
		@DisplayName( "Dado um ID inválido do veículo quando deleta da erro" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Dado um ID do veículo que não exista quando deleta da erro" )
		void givenNoExistedVehicle_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "A veículo a ser excluída não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Dado um ID do veículo que tenha relacionamento com itens então da erro" )
		void givenVehicleWithItemRelationship_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn(  Optional.ofNullable( new VehicleModel() ) );
			when( dao.hasProposalDetailRelationship( any() ) ).thenReturn( true );
			BusException e = assertThrows( BusException.class, ()->service.delete( 1, null ) );
			assertEquals( "Não é possível excluir o veículo pois existe um relacionamento com detalhe da proposta.", e.getMessage());
		}
	}
	
	@Nested
	class Find {
		@ParameterizedTest
		@MethodSource("com.portal.service.VehicleServiceTest#listEntityToFind")
		@DisplayName( "Quando busca retornar os IDs salvos" )
		void whenFind_ReturnProposal(VehicleModel mock, VehicleModel filter, int expectedSize ) throws Exception {
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( mock ) );
			
			List<VehicleModel> entityDB = dao.find( filter, null );
			
			assertNotNull( entityDB );
			assertFalse( entityDB.isEmpty() );
			assertEquals( expectedSize, entityDB.size() );
			
			Optional<VehicleModel> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
			
			assertNotNull( entity );
			assertTrue( entity.isPresent() );
			assertEquals( mock, entity.get() );
			assertEquals( mock.getId(), entity.get().getId() );
			assertEquals( mock.getChassi(), entity.get().getChassi() );
			assertEquals( mock.getPlate(), entity.get().getPlate() );
			assertNotNull( entity.get().getModel() );
			assertEquals( mock.getModel(), entity.get().getModel() );
			assertEquals( mock.getModelYear(), entity.get().getModelYear() );
			assertEquals( mock.getPurchaseDate(), entity.get().getPurchaseDate() );
			assertEquals( mock.getPurchaseValue(), entity.get().getPurchaseValue() );
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
		return Stream.of(
			Arguments.of(0, 3, "DESC", "vhe_id", 3),
			Arguments.of(0, 1, "DESC", "vhe_id", 3),
			Arguments.of(0, 1, "DESC", "mdl_id", 1),
			Arguments.of(0, 1, "DESC", "plate", 3),
			Arguments.of(1, 1, "DESC", "vhe_id", 2),
			Arguments.of(0, 1, "ASC", "vhe_id", 1)
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    		Arguments.of( new VehicleModel(null, null, null, null, null, null, null,null, null, null) ),
    		Arguments.of( new VehicleModel(1, "111111111111112", "aaa12345", modelMock, null, 2000, null, null, null, null) )

	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
		return Stream.of(
    		Arguments.of( new VehicleModel(null, null, null, null, null, null, null, null, null, null) ),
    		Arguments.of( new VehicleModel(0, "111111111111112", "aaa12345", modelMock, null, 2000, null, null, null, null) )
	    );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
		VehicleModel mock = new VehicleModel(1, "111111111111112", "aaa12345", modelMock,null, 2020, LocalDate.of(2019, 10, 10), 10000d, null, null);
		return Stream.of(
    		Arguments.of( mock, VehicleModel.builder().id(1).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().chassi( "111111111111112" ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().plate( "aaa12345" ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().model( modelMock ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().modelYear( mock.getModelYear() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().purchaseDate( mock.getPurchaseDate() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().purchaseValue( mock.getPurchaseValue() ).build(), 1 )
	    );
	}
}


