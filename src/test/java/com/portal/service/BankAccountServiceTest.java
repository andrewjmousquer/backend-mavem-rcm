package com.portal.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.portal.dao.impl.BankAccountDAO;
import com.portal.enums.AccountType;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Bank;
import com.portal.model.BankAccount;
import com.portal.model.Person;
import com.portal.service.imp.AuditService;
import com.portal.service.imp.BankAccountService;
import com.portal.service.imp.BankService;
import com.portal.service.imp.PersonService;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

@ExtendWith(SpringExtension.class)	
public class BankAccountServiceTest {

	@Mock
	BankAccountDAO dao;
	
	@Mock
	PersonService personService;
	
	@Mock
	BankService bankService;
	
	@Mock
	AuditService auditService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	BankAccountService service;
	
	private static final Person personMock = new Person( 1, "Person 1", null, null, "00000000000014", null, null, null, PersonClassification.PJ.getType() );
	private static final Bank bankMock = new Bank( 1, "Banco 1", "0001", true);
	
	@Nested
	class ListAll {
		@DisplayName("Listar as contas bancárias e retornar com sucesso a lista")
		@ParameterizedTest
		@MethodSource("com.portal.service.BankAccountServiceTest#whenListAllthenReturnEntityList")
		void whenListAll_thenReturnBankAccountList( int page, int size, String sortDir, String sort  ) throws Exception {
			when( dao.listAll( any() ) ).thenReturn( Arrays.asList( new BankAccount() ) );
			List<BankAccount> list = service.listAll( PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id") );
			assertFalse( list.isEmpty() );
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Salva uma nova conta bancária válido e retorna a conta com ID")
		void givenValidBankAccount_whenSave_thenReturnId() throws Exception {
			BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
			
			when( personService.getById( any() ) ).thenReturn( Optional.of( personMock ) );
			when( bankService.getById( any() ) ).thenReturn( Optional.of( bankMock ) );
			when( dao.find( any(), any() ) ).thenReturn( null );
			when( dao.save( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<BankAccount> entityDB = service.save( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getAgency(), entityDB.get().getAgency() );
			assertEquals( mock.getAccountNumber(), entityDB.get().getAccountNumber() );
			assertEquals( mock.getPixKey(), entityDB.get().getPixKey() );
			assertEquals( mock.getType(), entityDB.get().getType() );
			assertNotNull( entityDB.get().getBank() );
			assertEquals( mock.getBank(), entityDB.get().getBank() );
			assertEquals( mock.getBank().getName(), entityDB.get().getBank().getName() );
			assertEquals( mock.getBank().getCode(), entityDB.get().getBank().getCode() );
			assertEquals( mock.getBank().getActive(), entityDB.get().getBank().getActive() );
			assertNotNull( entityDB.get().getPerson() );
			assertEquals( mock.getPerson(), entityDB.get().getPerson() );
		}
		
		@DisplayName("Salva uma conta bancária e da erro nos validators. BNK-I1, BNK-I2")
		@ParameterizedTest
		@MethodSource( "com.portal.service.BankAccountServiceTest#invalidEntityDataToSaveValidator" )
		void givenInvalidBankAccount_whenSave_thenTestValidador( BankAccount model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
		    Set<ConstraintViolation<BankAccount>> violationSet = validator.validate( model, OnSave.class );
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}
	}
	
	@Nested
	class Update {
		@Test
		@DisplayName("Atualiza uma conta bancária válida e retorna com a atualização")
		void givenBankAccount_whenUpdate_thenReturnNewBank() throws Exception {
			BankAccount mock = new BankAccount(1, "00001-1", "000000001-1", "222222222-2", AccountType.CORRENTE, bankMock, personMock);
			
			when( personService.getById( any() ) ).thenReturn( Optional.of( personMock ) );
			when( bankService.getById( any() ) ).thenReturn( Optional.of( bankMock ) );
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new BankAccount() ) );
			when( dao.find( any(), any() ) ).thenReturn( Arrays.asList() );
			when( dao.update( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<BankAccount> entityDB = service.update( mock, null );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getAgency(), entityDB.get().getAgency() );
			assertEquals( mock.getAccountNumber(), entityDB.get().getAccountNumber() );
			assertEquals( mock.getPixKey(), entityDB.get().getPixKey() );
			assertEquals( mock.getType(), entityDB.get().getType() );
			assertNotNull( entityDB.get().getBank() );
			assertEquals( mock.getBank(), entityDB.get().getBank() );
			assertEquals( mock.getBank().getName(), entityDB.get().getBank().getName() );
			assertEquals( mock.getBank().getCode(), entityDB.get().getBank().getCode() );
			assertEquals( mock.getBank().getActive(), entityDB.get().getBank().getActive() );
			assertNotNull( entityDB.get().getPerson() );
			assertEquals( mock.getPerson(), entityDB.get().getPerson() );
		}
		
		@DisplayName("Atualiza uma conta bancária inválido e retorna erro. BNK-U1, BNK-U2")
		@ParameterizedTest
		@MethodSource( "com.portal.service.BankAccountServiceTest#invalidEntityDataToUpdateValidator" )
		void givenInvalidBankAccount_whenUpdate_thenTestValidador( BankAccount model ) throws Exception {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		    Validator validator = factory.getValidator();
		    Set<ConstraintViolation<BankAccount>> violationSet = validator.validate( model, OnUpdate.class );
		    assertFalse( violationSet.isEmpty(), "O correto é falhar, estamos validando o caminho ruim" );
		}

		@Test
		@DisplayName("Atualiza uma conta bancária não existente.")
		void givenNoExistBank_whenUpdate_thenReturnError_BNKU4() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BankAccount mock = new BankAccount(1, "00001-1", "000000001-1", "222222222-2", AccountType.CORRENTE, bankMock, personMock);
			BusException e = assertThrows( BusException.class, ()->service.update( mock, null ) );
			assertEquals( "A conta bancária a ser atualizado não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Delete {
		
		@Test
		@DisplayName( "Delete uma conta bancária com ID válido" )
		void givenValidId_whenDelete_thenNoError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.ofNullable( new BankAccount() ) );
			doNothing().when( spy(service) ).audit( null, null, null);
			assertDoesNotThrow( ()->service.delete(1, null) );
		}
		
		@Test
		@DisplayName( "Delete uma conta bancária com ID inválido" )
		void givenInvalidId_whenDelete_thenError() throws Exception {
			BusException e = assertThrows( BusException.class, ()->service.delete( null, null ) );
			assertEquals( "ID de exclusão inválido.", e.getMessage());
		}
		
		@Test
		@DisplayName( "Delete uma conta bancária com que não existe" )
		void givenNoExistedBank_whenDelete_thenError() throws Exception {
			when( dao.getById( any() ) ).thenReturn( Optional.empty() );
			BusException e = assertThrows( BusException.class, ()->service.delete( 999, null ) );
			assertEquals( "A conta bancária a ser excluída não existe.", e.getMessage());
		}
	}
	
	@Nested
	class Get {
		@Test
		@DisplayName( "Dado um ID uma conta bancária então retorna o objeto." )
		void givenBankAccountId_whenGetById_thenReturnEntity() throws AppException, BusException {
			BankAccount mock = new BankAccount(1, "00001-1", "000000001-1", "222222222-2", AccountType.CORRENTE, bankMock, personMock);
			
			when( personService.getById( any() ) ).thenReturn( Optional.of( personMock ) );
			when( bankService.getById( any() ) ).thenReturn( Optional.of( bankMock ) );
			when( dao.getById( any() ) ).thenReturn( Optional.of( mock ) );
			
			Optional<BankAccount> entityDB = service.getById( mock.getId() );
			
			assertNotNull( entityDB );
			assertTrue( entityDB.isPresent() );
			assertEquals( mock, entityDB.get() );
			assertEquals( mock.getAgency(), entityDB.get().getAgency() );
			assertEquals( mock.getAccountNumber(), entityDB.get().getAccountNumber() );
			assertEquals( mock.getPixKey(), entityDB.get().getPixKey() );
			assertEquals( mock.getType(), entityDB.get().getType() );
			assertNotNull( entityDB.get().getBank() );
			assertEquals( mock.getBank(), entityDB.get().getBank() );
			assertEquals( mock.getBank().getName(), entityDB.get().getBank().getName() );
			assertEquals( mock.getBank().getCode(), entityDB.get().getBank().getCode() );
			assertEquals( mock.getBank().getActive(), entityDB.get().getBank().getActive() );
			assertNotNull( entityDB.get().getPerson() );
			assertEquals( mock.getPerson(), entityDB.get().getPerson() );
		}
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
				Arguments.of(0, 1, "DESC", "id"),
				Arguments.of(0, 1, "DESC", null),
				Arguments.of(0, 1, "DESC", "act_id"),
				Arguments.of(0, 1, null, "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(0, 0, "DESC", "id"),
				Arguments.of(-1, 0, "DESC", "id")
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToSaveValidator() {
	    return Stream.of(
	    		Arguments.of( new BankAccount(null, null, null, null, null, null, null) ),
	    		Arguments.of( new BankAccount(0, "00001-1", "000000001-1", "222222222-2", AccountType.CORRENTE, bankMock, personMock) ),
	    		Arguments.of( new BankAccount(null, null, null, null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(null, null, null, null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", null, null, null, null, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", null, null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", null, null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(null, null, "00000000000000000000000000000000000000000000000000", null, null, null, null) ),
	    		Arguments.of( new BankAccount(null, null, "00000000000000000000000000000000000000000000000000", null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(null, null, "00000000000000000000000000000000000000000000000000", null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(null, null, null, "00000000000000000000000000000000000000000000000000", null, null, null) ),
	    		Arguments.of( new BankAccount(null, null, null, "00000000000000000000000000000000000000000000000000", null, bankMock, null) ),
	    		Arguments.of( new BankAccount(null, null, null, "00000000000000000000000000000000000000000000000000", AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, null, null, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(null, null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, null, null) ),
	    		Arguments.of( new BankAccount(null, null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, bankMock, null) ),
	    		Arguments.of( new BankAccount(null, null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", null, "00000000000000000000000000000000000000000000000000", null, null, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", null, "00000000000000000000000000000000000000000000000000", null, bankMock, null) ),
	    		Arguments.of( new BankAccount(null, "00000000000000000000000000000000000000000000000000", null, "00000000000000000000000000000000000000000000000000", AccountType.CORRENTE, null, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> invalidEntityDataToUpdateValidator() {
	    return Stream.of(
	    		Arguments.of( new BankAccount(1, null, null, null, null, null, null) ),
	    		Arguments.of( new BankAccount(null, "00001-1", "000000001-1", "222222222-2", AccountType.CORRENTE, bankMock, personMock) ),
	    		Arguments.of( new BankAccount(0, "00001-1", "000000001-1", "222222222-2", AccountType.CORRENTE, bankMock, personMock) ),
	    		Arguments.of( new BankAccount(1, null, null, null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(1, null, null, null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", null, null, null, null, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", null, null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", null, null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(1, null, "00000000000000000000000000000000000000000000000000", null, null, null, null) ),
	    		Arguments.of( new BankAccount(1, null, "00000000000000000000000000000000000000000000000000", null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(1, null, "00000000000000000000000000000000000000000000000000", null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(1, null, null, "00000000000000000000000000000000000000000000000000", null, null, null) ),
	    		Arguments.of( new BankAccount(1, null, null, "00000000000000000000000000000000000000000000000000", null, bankMock, null) ),
	    		Arguments.of( new BankAccount(1, null, null, "00000000000000000000000000000000000000000000000000", AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, null, null, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, null, bankMock, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(1, null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, null, null) ),
	    		Arguments.of( new BankAccount(1, null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", null, bankMock, null) ),
	    		Arguments.of( new BankAccount(1, null, "00000000000000000000000000000000000000000000000000", "00000000000000000000000000000000000000000000000000", AccountType.CORRENTE, null, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", null, "00000000000000000000000000000000000000000000000000", null, null, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", null, "00000000000000000000000000000000000000000000000000", null, bankMock, null) ),
	    		Arguments.of( new BankAccount(1, "00000000000000000000000000000000000000000000000000", null, "00000000000000000000000000000000000000000000000000", AccountType.CORRENTE, null, null) )
	    );
	}
}
