package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.portal.dao.impl.BrandDAO;
import com.portal.dao.impl.ModelDAO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ModelService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)
public class ModelServiceTest {

	@Mock
	ModelDAO dao;
	
	@Mock
	BrandDAO brandDAO;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	ModelService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os modelos e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.ModelServiceTest#whenListAllthenReturnModelList_Data")
		void whenListAll_thenReturnModelList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Model() ) );

			List<Model> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo modelo válido e retorna a marca com ID")
		void givenValidModel_whenSave_thenReturnId() throws Exception {
			
			when( brandDAO.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( Model.builder().id(1).build() ) );
			
			Model model = Model.builder()
								.name( "Model 1" )
								.active( true )
								.brand( Brand.builder().id(1).build() )
								.codFipe("038002-4")
								.build();
			
			Optional<Model> obj = service.save( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), new Integer(1) );
		}
		
		@DisplayName("Salva um modelo e da erro nos validators. MDL-I1, MDL-I3 e MDL-I4")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ModelServiceTest#invalidModelDataToSaveValidator" )
		void givenInvalidModel_whenSave_thenTestValidador( Model model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Model>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva uma nova marca duplicada com o mesmo nome. MDL-I2")
		void givenDuplicateModel_whenSave_thenReturnError_MDLI2() throws Exception {
			
			when( brandDAO.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( Model.builder().id( 1 ).build() ) );
			
			Model model = Model.builder()
								.name( "Model 1" )
								.brand( Brand.builder().id(1).build() )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um modelo com o mesmo nome e fabricante.", e.getMessage());
		}
		
		@Test
		@DisplayName("Salva um modelo com marca sem ID. MDL-I4")
		void givenInvalidBrandId_whenSave_thenReturnError_MDLI4() throws Exception {
			
			Model model = Model.builder()
								.name( "Model 1" )
								.brand( Brand.builder().build() )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "O fabricante associado é inválido ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Salva um modelo com marca não existente. MDL-I4")
		void givenNoExistBrand_whenSave_thenReturnError_MDLI4() throws Exception {
			
			when( brandDAO.getById( any() ) ).thenReturn( Optional.empty() );
			
			Model model = Model.builder()
								.name( "Model 1" )
								.brand( Brand.builder().build() )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "O fabricante associado é inválido ou não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um modelo válido e retorna com a atualização")
		void givenModel_whenUpdate_thenReturnNewModel() throws Exception {
			
			Model model = Model.builder()
								.id( 1 )
								.name( "Model 1.1" )
								.brand( Brand.builder().id(1).build() )
								.active( false )
								.build();
			
			when( brandDAO.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Model() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( model ) );
			
			Optional<Model> obj = service.update( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getName(), model.getName() );
			assertEquals( obj.get().getActive(), model.getActive() );
			assertEquals( obj.get().getBrand(), model.getBrand() );
		}
		
		@DisplayName("Atualiza um modelo inválido e retorna erro. MDL-U1, MDL-U3, MDL-U4")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ModelServiceTest#invalidModelDataToUpdateValidator" )
		void givenInvalidModel_whenUpdate_thenTestValidador( Model model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Model>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo modelo duplicada com o mesmo nome e fabricante. MDL-U2")
		void givenDuplicateModel_whenUpdate_thenReturnError_MDLU2() throws Exception {
			
			Model model = Model.builder()
					.id( 1 )
					.name( "Model 1" )
					.brand( Brand.builder().id(1).build() )
					.active( true )
					.build();
			
			Model duplicateModel = Model.builder()
					.id( 2 )
					.name( "Model 1" )
					.brand( Brand.builder().id(1).build() )
					.active( true )
					.build();

			when( brandDAO.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Model() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um modelo com o mesmo nome e fabricante.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um modelo existente e não pode dar erro de duplicado")
		void givenSelfModel_whenUpdate_thenNoError() throws Exception {
			
			Model model = Model.builder()
					.id( 1 )
					.name( "Model 1" )
					.brand( Brand.builder().id(1).build() )
					.active( true )
					.build();

			when( brandDAO.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Model() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um modelo não existente. MDL-U5")
		void givenNoExistModel_whenUpdate_thenReturnError_MDLU5() throws Exception {
			
			when( brandDAO.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			Model model = Model.builder()
								.id( 1 )
								.name( "Model 1" )
								.brand( Brand.builder().id(1).build() )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O modelo a ser atualizado não existe.", e.getMessage());
		}
		

		@Test
		@DisplayName("Atualiza um modelo com marca sem ID. MDL-U4")
		void givenInvalidBrandId_whenUpdate_thenReturnError_MDLU4() throws Exception {
			
			Model model = Model.builder()
								.name( "Model 1" )
								.brand( Brand.builder().build() )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "O fabricante associado é inválido ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Salva um modelo com fabricante não existente. MDL-U4")
		void givenNoExistBrand_whenSave_thenReturnError_MDLU4() throws Exception {
			
			when( brandDAO.getById( any() ) ).thenReturn( Optional.empty() );
			
			Model model = Model.builder()
								.name( "Model 1" )
								.brand( Brand.builder().build() )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "O fabricante associado é inválido ou não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete um modelo com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Model() ) );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( false );
			when( dao.hasItemRelationship( any() ) ).thenReturn( false );
			when( dao.hasProductRelationship( any() ) ).thenReturn( false );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um modelo com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um modelo que não existe" )
		void givenNoExistedModel_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O modelo a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um modelo com relacionamento com lead" )
		void givenModel_whenDelete_thenRelationshipLeadError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( Model.builder().id(1).build() ) );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( true );
			when( dao.hasItemRelationship( any() ) ).thenReturn( false );
			when( dao.hasProductRelationship( any() ) ).thenReturn( false );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir o modelo pois existe um relacionamento com lead.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um modelo com relacionamento com item" )
		void givenModel_whenDelete_thenRelationshipItemError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( Model.builder().id(1).build() ) );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( false );
			when( dao.hasItemRelationship( any() ) ).thenReturn( true );
			when( dao.hasProductRelationship( any() ) ).thenReturn( false );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir o modelo pois existe um relacionamento com item.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um modelo com relacionamento com produto" )
		void givenModel_whenDelete_thenRelationshipProductError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( Model.builder().id(1).build() ) );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( false );
			when( dao.hasItemRelationship( any() ) ).thenReturn( false );
			when( dao.hasProductRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir o modelo pois existe um relacionamento com produto.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um modelo com relacionamento com veículo" )
		void givenModel_whenDelete_thenRelationshipVehicleError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( Model.builder().id(1).build() ) );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( false );
			when( dao.hasItemRelationship( any() ) ).thenReturn( false );
			when( dao.hasProductRelationship( any() ) ).thenReturn( false );
			when( dao.hasVehicleRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir o modelo pois existe um relacionamento com veículo.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnModelList_Data() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "mdl_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidModelDataToSaveValidator() {
	    return Stream.of(
    			Arguments.of( new Model(null, null, null, null, null, null, null, null) ),
    			Arguments.of( new Model(0, "Model 1", true, Brand.builder().build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM ) ),
    			Arguments.of( new Model(null, "Model 1", null, null, null, null, null, null) ),
    			Arguments.of( new Model(null, "Model 1", true, null, null, null, null, null) ),
    			Arguments.of( new Model(null, "Model 1", null, Brand.builder().build(), null, null, null, null ) ),
    			Arguments.of( new Model(null, null, true, null, null, null, null, null) ),
    			Arguments.of( new Model(null, "Model 1", true, Brand.builder().build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, null ) ),
    			Arguments.of( new Model(null, "Model 1", true, Brand.builder().build(), null, ModelBodyType.HATCH, null, null ) ),
    			Arguments.of( new Model(null, "Model 1", true, Brand.builder().build(), null, null, null, null ) ),
    			Arguments.of( new Model(null, "Model 1", true, null, "038002-4", null, null, null ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidModelDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new Model(1, null, null, null, null, null, null, null) ),
	    		Arguments.of( new Model(0, "Model 1", true, Brand.builder().build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM) ),
	    		Arguments.of( new Model(null, "Model 1", true, Brand.builder().build(), null, ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM) ),
    			Arguments.of( new Model(1, "Model 1", null, null, null, null, null, null) ),
    			Arguments.of( new Model(1, "Model 1", true, null, null, null, null, null) ),
    			Arguments.of( new Model(1, "Model 1", null, Brand.builder().build(), null, null, null, null ) ),
    			Arguments.of( new Model(1, null, true, null, null, null, null, null) ),
    			Arguments.of( new Model(1, "Model 1", true, Brand.builder().build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, null ) ),
    			Arguments.of( new Model(1, "Model 1", true, Brand.builder().build(), "038002-4", ModelBodyType.HATCH, null, null ) ),
    			Arguments.of( new Model(1, "Model 1", true, Brand.builder().build(), null, null, null, null ) ),
    			Arguments.of( new Model(1, "Model 1", true, null, "038002-4", null, null, null ) )
	    );
	}
}
