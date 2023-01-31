package com.portal.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.portal.dao.impl.ProductDAO;
import com.portal.exceptions.BusException;
import com.portal.model.Product;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.ProductModelService;
import com.portal.service.imp.ProductService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class ProductServiceTest {

	@Mock
	ProductDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ProductModelService productModelService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	ProductService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os produtos e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.ProductServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnProductList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Product() ) );

			List<Product> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo produto válido e retorna a marca com ID")
		void givenValidProduct_whenSave_thenReturnId() throws Exception {
			Product model = new Product( null, "Product 1", true, 10, null );
			Product mock = new Product( 1, "Product 1", true, 10, null );
			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Product> entityDB = service.save( model, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( entityDB.get().getId(), mock.getId() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getId(), entityDB.get().getId() );
			assertEquals( mock.getName(), entityDB.get().getName() );
			assertEquals( mock.getActive(), entityDB.get().getActive() );
			assertEquals( mock.getProposalExpirationDays(), entityDB.get().getProposalExpirationDays() );
		}
		
		@DisplayName("Salva um produto e da erro nos validators. PRD-I1, PRD-I3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ProductServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidProduct_whenSave_thenTestValidador( Product model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Product>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo produto duplicado com o mesmo nome. PRD-I2")
		void givenDuplicateProduct_whenSave_thenReturnError_PRDI2() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( Product.builder().id( 1 ).build() ) );
			
			Product model = Product.builder()
								.name( "Product 1" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um produto com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um produto válido e retorna com a atualização")
		void givenProduct_whenUpdate_thenReturnNewProduct() throws Exception {
			Product mock = new Product( 1, "Product 1", true, 10, null );
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Product() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<Product> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( entityDB.get().getId(), mock.getId() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getId(), entityDB.get().getId() );
			assertEquals( mock.getName(), entityDB.get().getName() );
			assertEquals( mock.getActive(), entityDB.get().getActive() );
			assertEquals( mock.getProposalExpirationDays(), entityDB.get().getProposalExpirationDays() );
		}
		
		@DisplayName("Atualiza um produto inválido e retorna erro. PRD-U1, PRD-U3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.ProductServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidProduct_whenUpdate_thenTestValidador( Product model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Product>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo produto duplicado com o mesmo nome. PRD-U2")
		void givenDuplicateProduct_whenUpdate_thenReturnError_PRDU2() throws Exception {
			
			Product model = Product.builder()
					.id( 1 )
					.name( "Product 1" )
					.active( true )
					.build();

			Product duplicateModel = Product.builder()
					.id( 2 )
					.name( "Product 1" )
					.active( true )
					.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Product() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um produto com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza uma marca existente e não pode dar erro de duplicado")
		void givenSelfBrand_whenUpdate_thenNoError() throws Exception {
			
			Product model = Product.builder()
					.id( 1 )
					.name( "Product 1" )
					.active( true )
					.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Product() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um produto não existente. PRD-U4")
		void givenNoExistProduct_whenUpdate_thenReturnError_PRDU4() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			Product model = Product.builder()
								.id( 1 )
								.name( "Product 1" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O produto a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete um produto com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Product() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um produto com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um produto com que não existe" )
		void givenNoExistedCanal_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O produto a ser excluído não existe.", e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "prd_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    			Arguments.of( new Product(null, null, null, null, null) ),
    			Arguments.of( new Product(0, "Product 1", true, 10, null) ),
    			Arguments.of( new Product(null, "Product 1", true, null, null) ),
    			Arguments.of( new Product(null, null, null, 10, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new Product(1, null, null, null, null) ),
	    		Arguments.of( new Product(0, "Product 1", true, 10, null) ),
	    		Arguments.of( new Product(null, "Product 1", true, 10, null) ),
    			Arguments.of( new Product(1, "Product 1", null, 10, null) ),
    			Arguments.of( new Product(1, null, true, null, null) )
	    );
	}
}
