package com.portal.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import com.portal.dao.impl.PaymentMethodDAO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.PaymentMethodService;
import com.portal.service.imp.PaymentRuleService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PaymentMethodServiceTest {

	@Mock
	PaymentMethodDAO dao;
	
	@Mock
	PaymentRuleService pyrService;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PaymentMethodService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os métodos de pagamento e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.PaymentMethodServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnPaymentMethodList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( pyrService.find( any(), any()) ).thenReturn( Arrays.asList( PaymentRule.builder().id(1).build() ) );
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( PaymentMethod.builder().id(1).build() ) );

			List<PaymentMethod> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pym_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class GetById {

		@Test
		@DisplayName( "Dado um ID que contenha regras quando buscado por id retorna o objeto" )
		void givenIdWithRules_whenGetById_ReturnObject() throws AppException, BusException {
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( PaymentMethod.builder().id(1).build() ) );
			when( pyrService.find( any(), any()) ).thenReturn( Arrays.asList( PaymentRule.builder().id(1).build() ) );
			
			Optional<PaymentMethod> list = service.getById( 1 );
			
			assertNotNull( list );
			assertTrue( list.isPresent() );
			assertEquals( 1 , list.get().getId() );
//			assertNotNull( list.get().getRules() );
//			assertFalse( list.get().getRules().isEmpty() );
		}
		
		@Test
		@DisplayName( "Dado um ID que não contenha regras quando buscado por id retorna o objeto" )
		void givenIdWithoutRules_whenGetById_ReturnObject() throws AppException, BusException {
			
			when( dao.getById( any() ) ).thenReturn( Optional.of( PaymentMethod.builder().id(1).build() ) );
			when( pyrService.find( any(), any()) ).thenReturn( new ArrayList<>() );
			
			Optional<PaymentMethod> list = service.getById( 1 );
			
			assertNotNull( list );
			assertTrue( list.isPresent() );
			assertEquals( 1 , list.get().getId() );
//			assertTrue( list.get().getRules().isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo método de pagamento válido e retorna a marca com ID")
		void givenValidPaymentMethod_whenSave_thenReturnId() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( PaymentMethod.builder().id(1).build() ) );
			
			PaymentMethod model = PaymentMethod.builder()
								.name( "PaymentMethod 1" )
								.active( true )
								.build();
			
			Optional<PaymentMethod> obj = service.save( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), new Integer(1) );
		}
		
		@DisplayName("Salva um método de pagamento e da erro nos validators. PYM-I1, PYM-I3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PaymentMethodServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidPaymentMethod_whenSave_thenTestValidador( PaymentMethod model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PaymentMethod>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo método de pagamento duplicado com o mesmo nome. PYM-I2")
		void givenDuplicatePaymentMethod_whenSave_thenReturnError_CHNI2() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( PaymentMethod.builder().id( 1 ).build() ) );
			
			PaymentMethod model = PaymentMethod.builder()
								.name( "PaymentMethod 1" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um método de pagamento com o mesmo nome.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um método de pagamento válido e retorna com a atualização")
		void givenPaymentMethod_whenUpdate_thenReturnNewPaymentMethod() throws Exception {
			
			PaymentMethod model = PaymentMethod.builder()
								.id( 1 )
								.name( "PaymentMethod 1.1" )
								.active( false )
								.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PaymentMethod() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( model ) );
			
			Optional<PaymentMethod> obj = service.update( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getName(), model.getName() );
			assertEquals( obj.get().getActive(), model.getActive() );
		}
		
		@DisplayName("Atualiza um método de pagamento inválido e retorna erro. PYM-U1, PYM-U3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PaymentMethodServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidPaymentMethod_whenUpdate_thenTestValidador( PaymentMethod model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PaymentMethod>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo método de pagamento duplicado com o mesmo nome. PYM-U2")
		void givenDuplicatePaymentMethod_whenUpdate_thenReturnError_CHNU2() throws Exception {
			
			PaymentMethod model = PaymentMethod.builder()
										.id( 1 )
										.name( "PaymentMethod 1" )
										.active( true )
										.build();
			
			PaymentMethod duplicateModel = PaymentMethod.builder()
												.id( 2 )
												.name( "PaymentMethod 1" )
												.active( true )
												.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PaymentMethod() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um método de pagamento com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um método de pagamento existente e não pode dar erro de duplicado")
		void givenSelfBrand_whenUpdate_thenNoError() throws Exception {
			
			PaymentMethod model = PaymentMethod.builder()
										.id( 1 )
										.name( "PaymentMethod 1" )
										.active( true )
										.build();

			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PaymentMethod() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um método de pagamento não existente. PYM-U4")
		void givenNoExistPaymentMethod_whenUpdate_thenReturnError_CHNU4() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			PaymentMethod model = PaymentMethod.builder()
								.id( 1 )
								.name( "PaymentMethod 1" )
								.active( true )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O método de pagamento a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete um método de pagamento com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PaymentMethod() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um método de pagamento com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um método de pagamento com que não existe" )
		void givenNoExistedCanal_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O método de pagamento a ser excluído não existe.", e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "pym_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
    			Arguments.of( new PaymentMethod(null, null, null) ),
    			Arguments.of( new PaymentMethod(0, "PaymentMethod 1", true) ),
    			Arguments.of( new PaymentMethod(null, "PaymentMethod 1", null) ),
    			Arguments.of( new PaymentMethod(null, null, true) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new PaymentMethod(1, null, null) ),
	    		Arguments.of( new PaymentMethod(0, "PaymentMethod 1", true) ),
	    		Arguments.of( new PaymentMethod(null, "PaymentMethod 1", true) ),
    			Arguments.of( new PaymentMethod(1, "PaymentMethod 1", null) ),
    			Arguments.of( new PaymentMethod(1, null, true) )
	    );
	}
}
