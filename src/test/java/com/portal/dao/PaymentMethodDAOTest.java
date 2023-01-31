package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
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

import com.portal.dao.impl.PaymentMethodDAO;
import com.portal.model.PaymentMethod;

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
class PaymentMethodDAOTest {

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
	private PaymentMethodDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PaymentMethodDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( PaymentMethod entity ) throws Exception {
		Optional<PaymentMethod> entityId = dao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "listAll - Quando listar todos os métdos de pagamento" )
	void whenListAll_ReturnListChannel() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "pym_id");
		List<PaymentMethod> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	@DisplayName( "getById - Dado um ID existente retornar o método de pagamento" )
	void givenExistedChannel_whenGetById_ThenReturnChannel() throws Exception {
		
		PaymentMethod entity = PaymentMethod.builder()
								.id( 1 )
								.name( "PaymentMethod 1" )
								.active( true )
								.build();

		Optional<PaymentMethod> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "find - Dado um ID existente retornar o método de pagamento" )
	void givenExistedChannelId_whenFind_ThenReturnChannel() throws Exception {
		
		PaymentMethod entity = PaymentMethod.builder()
								.id( 1 )
								.build();

		List<PaymentMethod> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( entityDB.get(0).getId(), entity.getId() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "find - Dado um nome existente retornar o método de pagamento" )
	void givenExistedChannelName_whenFind_ThenReturnChannel() throws Exception {
		
		PaymentMethod entity = PaymentMethod.builder()
								.name( "PaymentMethod 1" )
								.build();

		List<PaymentMethod> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "find - Dado uma flag retornar o método de pagamento" )
	void givenExistedChannelActive_whenFind_ThenReturnChannel() throws Exception {
		
		PaymentMethod entity = PaymentMethod.builder()
								.active( true )
								.build();

		List<PaymentMethod> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "search - Dado um ID existente retornar o método de pagamento" )
	void givenExistedChannelId_whenSearch_ThenReturnChannel() throws Exception {
		
		PaymentMethod entity = PaymentMethod.builder()
									.id( 3 )
									.build();

		List<PaymentMethod> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( entityDB.get(0).getId(), entity.getId() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "search - Dado um nome existente retornar o método de pagamento" )
	void givenExistedChannelName_whenSearch_ThenReturnChannel() throws Exception {
		
		PaymentMethod entity = PaymentMethod.builder()
								.name( "PaymentMethod" )
								.build();

		List<PaymentMethod> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 3, entityDB.size() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "search - Dado uma flag retornar o método de pagamento" )
	void givenExistedChannelActive_whenSearch_ThenReturnChannel() throws Exception {
		PaymentMethod entity = PaymentMethod.builder()
								.active( false )
								.build();

		List<PaymentMethod> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PaymentMethodDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( PaymentMethod entity ) throws Exception {
		dao.update( entity );
		
		Optional<PaymentMethod> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
	}
	
	@Test
	@Order(4)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedChannel_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<PaymentMethod> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new PaymentMethod( null, "PaymentMethod 1", true ) ),
    		Arguments.of( new PaymentMethod( null, "PaymentMethod 2", true ) ),
    		Arguments.of( new PaymentMethod( null, "PaymentMethod 3", false ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new PaymentMethod( 1, "PaymentMethod 1.1", true ) ),
    		Arguments.of( new PaymentMethod( 2, "PaymentMethod 2", true ) ),
    		Arguments.of( new PaymentMethod( 3, "PaymentMethod 3.3", false ) )
	    );
	}
	
}
