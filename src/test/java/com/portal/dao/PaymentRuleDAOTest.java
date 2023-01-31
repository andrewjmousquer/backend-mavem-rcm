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
import com.portal.dao.impl.PaymentRuleDAO;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;

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
class PaymentRuleDAOTest {

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
	private PaymentRuleDAO dao;
	
	@Autowired
	private PaymentMethodDAO pymDao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PaymentRuleDAOTest#listPaymentMethodToSave")
	@DisplayName( "Salva os payments methods" )
	void whenSave_ReturnSavedId( PaymentMethod entity ) throws Exception {
		Optional<PaymentMethod> entityId = pymDao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PaymentRuleDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( PaymentRule entity ) throws Exception {
		Optional<PaymentRule> entityId = dao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "list - Quando listar todos as regras dos métodos de pagamentos" )
	void whenListAll_ReturnListPaymentRule() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "pyr_id");
		
		List<PaymentRule> list = dao.find( PaymentRule.builder().paymentMethod( PaymentMethod.builder().id(1).build() ).build(), pageReq  );

		assertEquals(3, list.size());
	}
	
	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar a regra do método de pagamento" )
	void givenExistedPaymentRule_whenGetById_ThenReturnPaymentRule() throws Exception {
		
		PaymentRule entity = PaymentRule.builder()
								.id( 1 )
								.installments( 3 )
								.tax( 1d )
								.build();

		Optional<PaymentRule> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PaymentRuleDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( PaymentRule entity ) throws Exception {
		dao.update( entity );
		
		Optional<PaymentRule> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
	}
	
	@Test
	@Order(4)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedPaymentRule_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<PaymentRule> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listPaymentMethodToSave() {
	    return Stream.of(
    		Arguments.of( new PaymentMethod( null, "PaymentMethod 1", true ) ),
    		Arguments.of( new PaymentMethod( null, "PaymentMethod 2", true ) ),
    		Arguments.of( new PaymentMethod( null, "PaymentMethod 3", false ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new PaymentRule( null, 3, 1.0, PaymentMethod.builder().id(1).build(), "PYR 1", true, true ) ),
    		Arguments.of( new PaymentRule( null, 5, 3.0, PaymentMethod.builder().id(1).build(), "PYR 2", true, true ) ),
    		Arguments.of( new PaymentRule( null, 10, 10.0, PaymentMethod.builder().id(1).build(), "PYR 3", true, true ) ),
    		Arguments.of( new PaymentRule( null, 1, 0.0, PaymentMethod.builder().id(2).build(), "PYR 4", true, true ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new PaymentRule( 1, 4, 1.5, PaymentMethod.builder().id(1).build(), "PYR 1", true, true ) ),
    		Arguments.of( new PaymentRule( 2, 5, 3.5, PaymentMethod.builder().id(1).build(), "PYR 2", true, true ) ),
    		Arguments.of( new PaymentRule( 3, 11, 10.0, PaymentMethod.builder().id(1).build(), "PYR 3", true, true ) ),
    		Arguments.of( new PaymentRule( 4, 2, 0.5, PaymentMethod.builder().id(2).build(), "PYR 4", true, true ) )
	    );
	}
	
}
