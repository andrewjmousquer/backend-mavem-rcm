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
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.BrandService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)
public class BrandServiceTest {

	@Mock
	BrandDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	BrandService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar as marcas e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.BrandServiceTest#whenListAllthenReturnBrandList_Data")
		void whenListAll_thenReturnBrandList( int page, int size, String sortDir, String sort  ) throws Exception {
			// Mocando o retorno dos métodos auxiliares
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Brand() ) );

			List<Brand> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva uma nova marca válida e retorna a marca com ID")
		void givenBrand_whenSave_thenReturnId() throws Exception {
			
			when( dao.save( any() ) ).thenReturn( Optional.of(Brand.builder().id(1).build()) );
			
			Brand brand = Brand.builder()
								.name( "BRAND 1" )
								.active( true )
								.build();
			
			Optional<Brand> obj = service.save( brand, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), new Integer(1) );
		}
		
		@DisplayName("Salva uma nova marca inválida e retorna erro. BRD-I1 e BRD-I3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.BrandServiceTest#invalidBrandDataToSave" )
		void givenInvalidBrand_whenSave_thenReturnError( Brand brand ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Brand>> violationSet = validator.validate( brand, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva uma nova marca duplicada com o mesmo nome. BRD-I2")
		void givenDuplicateBrand_whenSave_thenReturnError_BRDI2() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( Brand.builder().id( 1 ).build() ) );
			
			Brand brand = Brand.builder()
								.name( "BRAND 1" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( brand, null ) );
			assertEquals( "Já existe uma marca com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza uma marca válida e retorna com a atualização")
		void givenBrand_whenUpdate_thenReturnNewBrand() throws Exception {
			
			Brand newBrand = Brand.builder()
								.id( 1 )
								.name( "BRAND 1" )
								.active( true )
								.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Brand() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( newBrand ) );
			
			Brand brand = Brand.builder()
								.id( 1 )
								.name( "BRAND 1.1" )
								.active( false )
								.build();
			
			Optional<Brand> obj = service.update( brand, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getName(), newBrand.getName() );
			assertEquals( obj.get().getActive(), newBrand.getActive() );
		}
		
		@DisplayName("Atualiza uma marca inválida e retorna erro. BRD-U1 e BRD-U2")
		@ParameterizedTest
		@MethodSource( "com.portal.service.BrandServiceTest#invalidBrandDataToUpdate" )
		void givenInvalidBrand_whenUpdate_thenReturnError_BRDU1_BRDU2( Brand brand ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Brand>> violationSet = validator.validate( brand, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza uma nova marca duplicada com o mesmo nome. BRD-U3")
		void givenDuplicateBrand_whenUpdate_thenReturnError_BRDU3() throws Exception {
			
			Brand brand = Brand.builder()
									.id( 1 )
									.name( "BRAND 1" )
									.active( true )
									.build();
			
			Brand duplicateModel = Brand.builder()
									.id( 2 )
									.name( "BRAND 1" )
									.active( true )
									.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Brand() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( brand, null ) );
			assertEquals( "Já existe uma marca com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza uma marca existente e não pode dar erro de duplicado")
		void givenSelfBrand_whenUpdate_thenNoError() throws Exception {
			
			Brand model = Brand.builder()
								.id( 1 )
								.name( "BRAND 1" )
								.active( true )
								.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Brand() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza uma não existente. BRD-U4")
		void givenNoExistBrand_whenUpdate_thenReturnError_BRDU4() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable(null) );
			
			Brand brand = Brand.builder()
								.id( 100 )
								.name( "BRAND 100" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( brand, null ) );
			assertEquals( "A marca a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete uma marca com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Brand() ) );
			when( dao.hasModelRelationship( any() ) ).thenReturn( false );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( false );
			when( dao.hasPartnerRelationship( any() ) ).thenReturn( false );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete uma marca com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete uma marca com que não existe" )
		void givenNoExistedBrand_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "A marca a ser excluído não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete uma marca com relacionamento com model" )
		void givenBrand_whenDelete_thenRelationshipModelError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.hasModelRelationship( any() ) ).thenReturn( true );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( false );
			when( dao.hasPartnerRelationship( any() ) ).thenReturn( false );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir a marca pois existe um relacionamento com modelo.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete uma marca com relacionamento com lead" )
		void givenBrand_whenDelete_thenRelationshipLeadError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.hasModelRelationship( any() ) ).thenReturn( false );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( true );
			when( dao.hasPartnerRelationship( any() ) ).thenReturn( false );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir a marca pois existe um relacionamento com lead.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete uma marca com relacionamento com parceiro" )
		void givenBrand_whenDelete_thenRelationshipPartnerError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.of( Brand.builder().id(1).build() ) );
			when( dao.hasModelRelationship( any() ) ).thenReturn( false );
			when( dao.hasLeadRelationship( any() ) ).thenReturn( false );
			when( dao.hasPartnerRelationship( any() ) ).thenReturn( true );
			
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "Não é possível excluir a marca pois existe um relacionamento com parceiro.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnBrandList_Data() {
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
	private static Stream<Arguments> invalidBrandDataToSave() {
	    return Stream.of(
	    			Arguments.of( new Brand(null, null, null) ),
	    			Arguments.of( new Brand(0, "BRAND 1", true) ),
	    			Arguments.of( new Brand(1, "BRAND 1", true) ),
	    			Arguments.of( new Brand(null, "BRAND 1", null) ),
	    			Arguments.of( new Brand(null, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", true) ),
	    			Arguments.of( new Brand(null, null, true) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidBrandDataToUpdate() {
	    return Stream.of(
	    			Arguments.of( new Brand(null, null, null) ),
	    			Arguments.of( new Brand(1, null, null) ),
	    			Arguments.of( new Brand(1, "BRAND 1", null) ),
	    			Arguments.of( new Brand(null, "BRAND 1", null) ),
	    			Arguments.of( new Brand(0, "BRAND 1", true) ),
	    			Arguments.of( new Brand(null, "BRAND 1", true) ),
	    			Arguments.of( new Brand(null, null, true) ),
	    			Arguments.of( new Brand(1, null, true) )
	    );
	}
}
