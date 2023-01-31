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

import com.portal.dao.impl.BankDAO;
import com.portal.model.Bank;

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
class BankDAOTest {

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
	private BankDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.BankDAOTest#listBankToSave")
	void whenSave_ReturnSavedId( Bank bank ) throws Exception {
		Optional<Bank> bankId = dao.save( bank );
		assertNotNull( bankId );
		assertTrue( bankId.isPresent() );
		assertEquals( bank.getId(), bankId.get().getId() );
	}
	
	@Test
	@Order(2)
	void whenListAll_ReturnListBank() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "bnk_id");
		List<Bank> list = dao.listAll( pageReq  );

		assertEquals(listBankToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	void givenExistedBank_whenGetById_ThenReturnBank() throws Exception {
		
		Bank bank = Bank.builder()
								.id( 1 )
								.name( "Bank 1" )
								.code( "Code 1" )
								.build();

		Optional<Bank> bankDB = dao.getById( bank.getId() );
		
		assertNotNull( bankDB );
		assertTrue( bankDB.isPresent() );
		assertEquals( bank, bankDB.get() );
	}
	
	@Test
	@Order(2)
	void givenExitedBankId_whenFind_ThenReturnBank() throws Exception {
		
		Bank bank = Bank.builder()
								.id( 1 )
								.build();

		List<Bank> bankDB = dao.find( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( bankDB.get(0).getId(), bank.getId() );
	}
	
	@Test
	@Order(2)
	void givenExitedBankName_whenFind_ThenReturnBank() throws Exception {
		
		Bank bank = Bank.builder()
								.name( "Bank 1" )
								.build();

		List<Bank> bankDB = dao.find( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( 1, bankDB.size() );
	}
	
	@Test
	@Order(2)
	void givenExitedBankCode_whenFind_ThenReturnBank() throws Exception {
		
		Bank bank = Bank.builder()
								.code( "Code 1" )
								.build();

		List<Bank> bankDB = dao.find( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( 1, bankDB.size() );
	}
	
	@Test
	@Order(2)
	void givenExistedBankActive_whenFind_ThenReturnBank() throws Exception {
		
		Bank bank = Bank.builder()
						.active( true )
						.build();

		List<Bank> bankDB = dao.find( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( 3, bankDB.size() );
	}
	
	
	@Test
	@Order(2)
	void givenExitedBankId_whenSearch_ThenReturnBank() throws Exception {
		
		Bank bank = Bank.builder()
								.id( 3 )
								.build();

		List<Bank> bankDB = dao.search( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( bankDB.get(0).getId(), bank.getId() );
	}
	
	@Test
	@Order(2)
	void givenExitedBankName_whenSearch_ThenReturnBank() throws Exception {
		
		Bank bank = Bank.builder()
								.name( "Bank" )
								.build();

		List<Bank> bankDB = dao.search( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( 3, bankDB.size() );
	}
	
	
	@Test
	@Order(2)
	void givenExitedBankCode_whenSearch_ThenReturnBank() throws Exception {
		Bank bank = Bank.builder()
							.code( "Code" )
							.build();

		List<Bank> bankDB = dao.search( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( 3, bankDB.size() );
	}
	
	@Test
	@Order(2)
	void givenExistedBankActive_whenSearch_ThenReturnChannel() throws Exception {
		Bank bank = Bank.builder()
						.active( true )
						.build();

		List<Bank> bankDB = dao.search( bank, null );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isEmpty() );
		assertEquals( 3, bankDB.size() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.BankDAOTest#listBankToUpdate")
	void whenUpdate_CheckNewValues( Bank bank ) throws Exception {
		dao.update( bank );
		
		Optional<Bank> bankDB = dao.getById( bank.getId() );
		
		assertNotNull( bankDB );
		assertTrue( bankDB.isPresent() );
		assertEquals( bank, bankDB.get() );
	}
	
	@Test
	@Order(4)
	void givenExitedBank_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Bank> bankDB = dao.getById( id );
		
		assertNotNull( bankDB );
		assertFalse( bankDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listBankToSave() {
	    return Stream.of(
	    		Arguments.of( new Bank( null, "Bank 1", "Code 1", true ) ),
	    		Arguments.of( new Bank( null, "Bank 2", "Code 2", true ) ),
	    		Arguments.of( new Bank( null, "Bank 3", "Code 3", true ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listBankToUpdate() {
	    return Stream.of(
	    		Arguments.of( new Bank( 1, "Bank 1.1", "Code 1", true ) ),
	    		Arguments.of( new Bank( 2, "Bank 2", "Code 2.2", true ) ),
	    		Arguments.of( new Bank( 3, "Bank 3.1", "Code 3", true ) )
	    );
	}
	
}
