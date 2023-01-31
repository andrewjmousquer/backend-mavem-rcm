package com.portal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
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
import com.portal.dao.impl.BankDAO;
import com.portal.exceptions.BusException;
import com.portal.model.Bank;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.BankService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class BankServiceTest {

	@Mock
	BankDAO dao;
	
	@Mock
	AuditService auditService;
	
	@Mock
	MessageSource messageSource;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@InjectMocks
	BankService service;
	
	@Nested
	class ListAll {
		@DisplayName("Listar os bancos e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.BankServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnBankList( int page, int size, String sortDir, String sort ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new Bank() ) );

			List<Bank> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva um novo banco válido e retorna a banco com ID")
		void givenValidBank_whenSave_thenReturnId() throws Exception {
			
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( Bank.builder().id(1).build() ) );
			
			Bank model = Bank.builder()
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();
			
			Optional<Bank> obj = service.save( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( obj.get().getId(), new Integer(1) );
		}
		
		@DisplayName("Salva um banco e da erro nos validators. BNK-I1, BNK-I3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.BankServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidBank_whenSave_thenTestValidador( Bank model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Bank>> violationSet = validator.validate( model, OnSave.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Salva um novo banco duplicado com o mesmo nome. BNK-I2")
		void givenDuplicateBankName_whenSave_thenReturnError_BNKI2() throws Exception {
			
			Bank model = Bank.builder()
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();
			
			Bank duplicateModel = Bank.builder()
									.id( 2 )
									.name( "Bank 1" )
									.code( "Code 2" )
									.build();

			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um banco com o mesmo nome.", e.getMessage());
		}
		
		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("Salva um novo banco duplicado com o mesmo código. BNK-I4")
		void givenDuplicateBankCode_whenSave_thenReturnError_BNKI4() throws Exception {
			
			Bank model = Bank.builder()
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();
			
			Bank duplicateModel = Bank.builder()
									.id( 2 )
									.name( "Bank 2" )
									.code( "Code 1" )
									.build();
			
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList(), Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.save( model, null ) );
			assertEquals( "Já existe um banco com o mesmo código.", e.getMessage());
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza um banco válido e retorna com a atualização")
		void givenBank_whenUpdate_thenReturnNewBank() throws Exception {
			
			Bank model = Bank.builder()
								.id( 1 )
								.name( "Bank 1.1" )
								.code( "Code 2" )
								.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Bank() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( model ) );
			
			Optional<Bank> obj = service.update( model, null );
			
			assertTrue( obj.isPresent() );
			assertEquals( model.getName(), obj.get().getName() );
			assertEquals( model.getCode(), obj.get().getCode() );
		}
		
		@DisplayName("Atualiza um banco inválido e retorna erro. BNK-U1, BNK-U3")
		@ParameterizedTest
		@MethodSource( "com.portal.service.BankServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidBank_whenUpdate_thenTestValidador( Bank model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
			
		    Set<ConstraintViolation<Bank>> violationSet = validator.validate( model, OnUpdate.class );
		    
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza um novo banco duplicado com o mesmo nome. BNK-U2")
		void givenDuplicateBank_whenUpdate_thenReturnError_BNKU2() throws Exception {
			
			Bank model = Bank.builder()
								.id( 1 )
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();
			
			Bank duplicateModel = Bank.builder()
								.id( 2 )
								.name( "Bank 1" )
								.code( "Code 2" )
								.build();
			
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Bank() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um banco com o mesmo nome.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza o próprio bance não pode dar erro de duplicado")
		void givenSelfBank_whenUpdate_thenNoError() throws Exception {

			Bank model = Bank.builder()
								.id( 1 )
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Bank() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( model ) ); // Para o método de validation
			
			assertDoesNotThrow( ()->service.update( model, null ) );
		}
		
		@Test
		@DisplayName("Atualiza um novo banco duplicado com o mesmo código. BNK-U5")
		void givenDuplicateBankCode_whenUpdate_thenReturnError_BNKU5() throws Exception {
			
			Bank model = Bank.builder()
								.id( 1 )
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();
			
			Bank duplicateModel = Bank.builder()
									.id( 2 )
									.name( "Bank 2" )
									.code( "Code 1" )
									.build();
			
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Bank() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList( duplicateModel ) );
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "Já existe um banco com o mesmo código.", e.getMessage());
		}
		
		@Test
		@DisplayName("Atualiza um banco não existente. BNK-U4")
		void givenNoExistBank_whenUpdate_thenReturnError_BNKU4() throws Exception {
			
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			
			Bank model = Bank.builder()
								.id( 1 )
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();
			
			BusException e = assertThrows( BusException.class, ()->service.update( model, null ) );
			assertEquals( "O banco a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete um banco com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new Bank() ) );
			doNothing().when( spy(service) ).audit( null, null, null);
			
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete um banco com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete um banco com que não existe" )
		void givenNoExistedBank_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "O banco a ser excluído não existe.", e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
			Arguments.of(0, 1, "DESC", "id"),
			Arguments.of(0, 1, "DESC", null),
			Arguments.of(0, 1, "DESC", "bnk_id"),
			Arguments.of(0, 1, null, "id"),
			Arguments.of(0, 0, "DESC", "id"),
			Arguments.of(0, 0, "DESC", "id"),
			Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
			Arguments.of( new Bank(null, null, null, null) ),
			Arguments.of( new Bank(0, "Bank 1", "Code 1", true) ),
			Arguments.of( new Bank(null, "Bank 1", null, null) ),
			Arguments.of( new Bank(null, "Bank 1", null, true) ),
			Arguments.of( new Bank(null, null, "Code 1", null) ),
			Arguments.of( new Bank(null, null, "Code 1", true) ),
			Arguments.of( new Bank(null, null, null, true) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
    			Arguments.of( new Bank(1, null, null, null) ),
    			Arguments.of( new Bank(0, "Bank 1", "Code 1", true) ),
    			Arguments.of( new Bank(null, "Bank 1", "Code 1", true) ),
    			Arguments.of( new Bank(1, "Bank 1", null, null) ),
    			Arguments.of( new Bank(1, "Bank 1", null, true) ),
    			Arguments.of( new Bank(1, null, "Code 1", null) ),
    			Arguments.of( new Bank(1, null, "Code 1", true) ),
    			Arguments.of( new Bank(1, null, null, true) )
	    );
	}
}
