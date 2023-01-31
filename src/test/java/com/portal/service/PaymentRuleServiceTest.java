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
import com.portal.dao.impl.PaymentRuleDAO;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.PaymentMethodService;
import com.portal.service.imp.PaymentRuleService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class PaymentRuleServiceTest {

	@Mock
	PaymentRuleDAO dao;
	
	@Mock
	PaymentMethodService pymService;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PaymentRuleService service;
	
	@Nested
	class ListByPym {
		@DisplayName("Listar as regras de um método de pagamento e retorna com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.PaymentRuleServiceTest#whenListAllthenReturnEntityList")
		void givenPymId_whenListAll_thenReturnPaymentRuleList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( new PaymentRule() ) );

			List<PaymentRule> list = service.find( PaymentRule.builder().paymentMethod( PaymentMethod.builder().id(1).build() ).build(), PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pym_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva uma nova regra válida e retorna a marca com ID")
		void givenValidPaymentRule_whenSave_thenReturnId() throws Exception {
			when( dao.save( any() ) ).thenReturn( Optional.of( PaymentRule.builder().id(1).build() ) );
			when( pymService.getById( any() ) ).thenReturn( Optional.of(PaymentMethod.builder().id(1).build()) );
			
			PaymentRule model = PaymentRule.builder()
								.installments( 3 )
								.tax( 1.0 )
								.paymentMethod( PaymentMethod.builder().id(1).build() )
								.build();
			
			Optional<PaymentRule> obj = service.save( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), new Integer(1) );
		}
		
		@DisplayName("Salva uma regra e da erro nos validators. PYR-I1,PYR-I2,PYR-I3,PYR-I4,PYR-I5")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PaymentRuleServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidPaymentRule_whenSave_thenTestValidador( PaymentRule model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
		    Set<ConstraintViolation<PaymentRule>> violationSet = validator.validate( model, OnSave.class );
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva uma nova regra sem e existência de uma método de pagamento")
		void givenPaymentRuleWithoutPym_whenSave_thenReturnError() throws Exception {
			
			when( pymService.getById( any() ) ).thenReturn( Optional.empty() );
			
			PaymentRule model = PaymentRule.builder()
								.installments( 1 )
								.tax( 1d )
								.paymentMethod( PaymentMethod.builder().id(11).build() )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "O método de pagamento é inválido ou não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza uma regra válida e retorna com a atualização")
		void givenPaymentRule_whenUpdate_thenReturnNewPaymentRule() throws Exception {

			PaymentRule model = PaymentRule.builder()
									.id(1)
									.installments( 1 )
									.tax( 1d )
									.paymentMethod( PaymentMethod.builder().id(11).build() )
									.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PaymentRule() ) );
			when( dao.update( any() ) ).thenReturn( Optional.of( model ) );
			when( pymService.getById( any() ) ).thenReturn( Optional.of(PaymentMethod.builder().id(1).build()) );
			Optional<PaymentRule> obj = service.update( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getInstallments(), model.getInstallments() );
			assertEquals( obj.get().getTax(), model.getTax() );
		}
		
		@DisplayName("Atualiza uma regra inválida e retorna erro. PYR-U1,PYR-U2,PYR-U3,PYR-U4,PYR-U5")
		@ParameterizedTest
		@MethodSource( "com.portal.service.PaymentRuleServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidPaymentRule_whenUpdate_thenTestValidador( PaymentRule model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<PaymentRule>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva uma nova regra sem e existência de uma método de pagamento")
		void givenDuplicatePaymentRule_whenUpdate_thenReturnError_CHNU2() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PaymentRule() ) );
			when( pymService.getById( any() ) ).thenReturn( Optional.empty() );
			
			PaymentRule model = PaymentRule.builder()
								.installments( 1 )
								.tax( 1d )
								.paymentMethod( PaymentMethod.builder().id(11).build() )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "O método de pagamento é inválido ou não existe.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza uma regra que não existente. PYR-U6")
		void givenNoExistPaymentRule_whenUpdate_thenReturnError_CHNU4() throws Exception {
			
			when( pymService.getById( any() ) ).thenReturn( Optional.of(PaymentMethod.builder().id(1).build()) );
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			PaymentRule model = PaymentRule.builder()
											.id(1)
											.installments( 1 )
											.tax( 1d )
											.paymentMethod( PaymentMethod.builder().id(11).build() )
											.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "A regra a ser atualizada não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Deleta uma regra com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new PaymentRule() ) );
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Deleta uma regra com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Deleta uma regra que não existe.PYR-D7" )
		void givenNoExistedCanal_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "A regra a ser excluída não existe.", e.getMessage());
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
    			Arguments.of( new PaymentRule(null, null, null, null, null, null, null) ),
    			Arguments.of( new PaymentRule(0, 1, 1d, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, 1, null, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, 1, 1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, null, 1.0, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, 1, null, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, -1, null, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, -1, 1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, -1, 1.0, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, null, -1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, 1, -1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(null, 1, -1.0, new PaymentMethod(), "PYR 1", true, true) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new PaymentRule(1, null, null, null, null, null, null) ),
	    		Arguments.of( new PaymentRule(0, 1, 1d, new PaymentMethod(), "PYR 1", true, true) ),
	    		Arguments.of( new PaymentRule(null, 1, 1d, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, 1, null, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, 1, 1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, null, 1.0, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, 1, null, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, -1, null, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, -1, 1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, -1, 1.0, new PaymentMethod(), "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, null, -1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, 1, -1.0, null, "PYR 1", true, true) ),
    			Arguments.of( new PaymentRule(1, 1, -1.0, new PaymentMethod(), "PYR 1", true, true) )
	    );
	}
}
