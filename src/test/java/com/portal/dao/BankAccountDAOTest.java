package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dao.impl.BankAccountDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AccountType;
import com.portal.enums.PersonClassification;
import com.portal.model.Bank;
import com.portal.model.BankAccount;
import com.portal.model.Person;
import com.portal.model.UserModel;
import com.portal.service.imp.BankService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PersonService;

/**
 * Quando se trata de test de DAO, todos os teste devem ser executados na ordem correta,
 * todos são criados usando uma ordem lógica com base no registro salvo no DB.
 * Isso por que esse tipo de teste é de integração, então ele leva em consideração os dados previamento inseridos.
 * 
 * @author Brunno Tavares
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class BankAccountDAOTest {

	@Container
	private static final MySQLContainer<?> database = new MySQLContainer<>( "mysql:5.7.34" )
																.withInitScript("1-structure.sql")
																.withDatabaseName("carbon");
	
	
	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
	    registry.add("spring.datasource-portal.jdbcUrl", database::getJdbcUrl);
	    registry.add("spring.datasource-portal.username", database::getUsername);
	    registry.add("spring.datasource-portal.password", database::getPassword);
	}
	
	@Autowired
	private BankAccountDAO dao;
	
	@Autowired
	private ClassifierService classifierService;
	
	@Autowired
	private BankService bankService;
	
	@Autowired
	private PersonService personService;
	private static final Person personMock = new Person(null, "Person 1", null, null, "00000000000014", null, null, null, PersonClassification.PF.getType());
	private static final Bank bankMock = new Bank( null, "Banco 1", "0001", true);
	
	/**
	 * Devido a injeção de dependências não é possivil usar o @BerforeAll
	 * Por esse motivo forçamos ser o primeiro passo do teste a inserção dos dados 
	 * usados como base.
	 */
	@Test
	@Order(1)
	void setup() throws Exception {
		UserProfileDTO profile = new UserProfileDTO( new UserModel( "MOCK USER" ) );

		for( PersonClassification classifiers : PersonClassification.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}
		
		for( AccountType classifiers : AccountType.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}

		Optional<Person> dbPerson = personService.save(personMock, profile);
		personMock.setId( dbPerson.get().getId() );
		
		Optional<Bank> dbBank = bankService.save(bankMock, profile);
		bankMock.setId( dbBank.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.BankAccountDAOTest#listEntityToSave")
	void whenSave_ReturnSavedId( BankAccount entity ) throws Exception {
		Optional<BankAccount> entityDB = dao.save( entity );
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity.getId(), entityDB.get().getId() );
	}
	
	@Test
	@Order(3)
	void whenListAll_ReturnListBankAccount() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "act_id");
		List<BankAccount> list = dao.listAll( pageReq  );
		assertEquals( listEntityToSave().count(), list.size() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountId_whenGetById_ThenReturnBankAccount() throws Exception {
		
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);

		Optional<BankAccount> entityDB = dao.getById( mock.getId() );
		
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
	
	@Test
	@Order(3)
	void givenBankAccountId_whenFind_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);

		List<BankAccount> entityDB = dao.find( BankAccount.builder().id( 1 ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		
		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountAgency_whenFind_ThenReturnBankAccount() throws Exception {
		
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.find( BankAccount.builder().agency( "00001" ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountAccount_whenFind_ThenReturnBankAccount() throws Exception {
		
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.find( BankAccount.builder().accountNumber( "000000001" ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountPix_whenFind_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.find( BankAccount.builder().pixKey( "222222222" ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountBank_whenFind_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.find( BankAccount.builder().bank( bankMock ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 4, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountType_whenFind_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.find( BankAccount.builder().type( AccountType.CORRENTE ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 3, entityDB.size() );
		
		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountAll_whenFind_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.find( mock, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountPerson_whenFind_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.find( BankAccount.builder().person( personMock ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 4, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}

	@Test
	@Order(4)
	void givenBankAccountId_whenSearch_ThenReturnBankAccount() throws Exception {
		
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);

		List<BankAccount> entityDB = dao.search( BankAccount.builder().id( 1 ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(4)
	void givenBankAccountAgency_whenSearch_ThenReturnBankAccount() throws Exception {
		
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.search( BankAccount.builder().agency( "1" ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(4)
	void givenBankAccountAccount_whenSearch_ThenReturnBankAccount() throws Exception {
		
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.search( BankAccount.builder().accountNumber( "1" ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(4)
	void givenBankAccountPix_whenSearch_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.search( BankAccount.builder().pixKey( "22" ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(4)
	void givenBankAccountBank_whenSearch_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.search( BankAccount.builder().bank( bankMock ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 4, entityDB.size() );
		

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(4)
	void givenBankAccountType_whenSearch_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.search( BankAccount.builder().type( AccountType.CORRENTE ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 3, entityDB.size() );
		

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountAll_whenSearch_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.search( mock, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Test
	@Order(3)
	void givenBankAccountPerson_whenSearch_ThenReturnBankAccount() throws Exception {
		BankAccount mock = new BankAccount(1, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock);
		
		List<BankAccount> entityDB = dao.search( BankAccount.builder().person(personMock).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 4, entityDB.size() );

		Optional<BankAccount> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getAgency(), entity.get().getAgency() );
		assertEquals( mock.getAccountNumber(), entity.get().getAccountNumber() );
		assertEquals( mock.getPixKey(), entity.get().getPixKey() );
		assertEquals( mock.getType(), entity.get().getType() );
		
		assertNotNull( entity.get().getBank() );
		assertEquals( mock.getBank(), entity.get().getBank() );
		assertEquals( mock.getBank().getName(), entity.get().getBank().getName() );
		assertEquals( mock.getBank().getCode(), entity.get().getBank().getCode() );
		assertEquals( mock.getBank().getActive(), entity.get().getBank().getActive() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mock.getPerson(), entity.get().getPerson() );
	}
	
	@Order(5)
	@ParameterizedTest
	@MethodSource("com.portal.dao.BankAccountDAOTest#listEntityToUpdate")
	void whenUpdate_CheckNewValues( BankAccount entity ) throws Exception {
		dao.update( entity );
		
		Optional<BankAccount> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getAgency(), entityDB.get().getAgency() );
		assertEquals( entity.getAccountNumber(), entityDB.get().getAccountNumber() );
		assertEquals( entity.getPixKey(), entityDB.get().getPixKey() );
		assertEquals( entity.getType(), entityDB.get().getType() );
		assertNotNull( entityDB.get().getBank() );
		assertEquals( entity.getBank(), entityDB.get().getBank() );
		assertEquals( entity.getBank().getName(), entityDB.get().getBank().getName() );
		assertEquals( entity.getBank().getCode(), entityDB.get().getBank().getCode() );
		assertEquals( entity.getBank().getActive(), entityDB.get().getBank().getActive() );
		assertNotNull( entityDB.get().getPerson() );
		assertEquals( entity.getPerson(), entityDB.get().getPerson() );
	}
	
	@Test
	@Order(6)
	void givenExitedBankAccount_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<BankAccount> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new BankAccount(null, "00001", "000000001", "222222222", AccountType.CORRENTE, bankMock, personMock) ),
    		Arguments.of( new BankAccount(null, "00002", "000000002", null, AccountType.CORRENTE, bankMock, personMock) ),
    		Arguments.of( new BankAccount(null, "00003", null, null, AccountType.POUPANCA, bankMock, personMock) ),
    		Arguments.of( new BankAccount(null, null, null, "4444444", AccountType.CORRENTE, bankMock, personMock) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new BankAccount(1, "00001-1", "000000001-1", "222222222-1", AccountType.CORRENTE, bankMock, personMock) ),
    		Arguments.of( new BankAccount(2, "00002-2", "000000002", null, AccountType.POUPANCA, bankMock, personMock) ),
    		Arguments.of( new BankAccount(3, "00003", "33", null, AccountType.POUPANCA, bankMock, personMock) ),
    		Arguments.of( new BankAccount(4, "44", null, null, AccountType.CORRENTE, bankMock, personMock) )
	    );
	}
	
}
