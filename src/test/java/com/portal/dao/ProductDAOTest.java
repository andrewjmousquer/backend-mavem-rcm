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

import com.portal.dao.impl.ProductDAO;
import com.portal.model.Product;

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
class ProductDAOTest {

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
	private ProductDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ProductDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Product entity ) throws Exception {
		Optional<Product> entityId = dao.save( entity );
		
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
		assertEquals( entity.getName(), entityId.get().getName() );
		assertEquals( entity.getActive(), entityId.get().getActive() );
		assertEquals( entity.getProposalExpirationDays(), entityId.get().getProposalExpirationDays() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "listAll - Quando listar todos os produtos" )
	void whenListAll_ReturnListProduct() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "prd_id");
		List<Product> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	@DisplayName( "getById - Dado um ID existente retornar o produto" )
	void givenExistedProduct_whenGetById_ThenReturnProduct() throws Exception {
		Product mock = new Product( 1, "Product 1", true, 10, null );

		Optional<Product> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getId(), entityDB.get().getId() );
		assertEquals( mock.getName(), entityDB.get().getName() );
		assertEquals( mock.getActive(), entityDB.get().getActive() );
		assertEquals( mock.getProposalExpirationDays(), entityDB.get().getProposalExpirationDays() );

	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ProductDAOTest#listEntityToFind")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenFind_ReturnProposal( Product mock, Product filter, int expectedSize ) throws Exception {
		List<Product> entityDB = dao.find( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<Product> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertNotNull( entity );
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getId(), entity.get().getId() );
		assertEquals( mock.getName(), entity.get().getName() );
		assertEquals( mock.getActive(), entity.get().getActive() );
		assertEquals( mock.getProposalExpirationDays(), entity.get().getProposalExpirationDays() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ProductDAOTest#listEntityToSearch")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenSearch_ReturnProposal( Product mock, Product filter, int expectedSize ) throws Exception {
		List<Product> entityDB = dao.search( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<Product> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertNotNull( entity );
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getId(), entity.get().getId() );
		assertEquals( mock.getName(), entity.get().getName() );
		assertEquals( mock.getActive(), entity.get().getActive() );
		assertEquals( mock.getProposalExpirationDays(), entity.get().getProposalExpirationDays() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ProductDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( Product entity ) throws Exception {
		dao.update( entity );
		
		Optional<Product> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getId(), entityDB.get().getId() );
		assertEquals( entity.getName(), entityDB.get().getName() );
		assertEquals( entity.getActive(), entityDB.get().getActive() );
		assertEquals( entity.getProposalExpirationDays(), entityDB.get().getProposalExpirationDays() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedProduct_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Product> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Product( null, "Product 1", true, 10, null ) ),
    		Arguments.of( new Product( null, "Product 2", true, 5, null ) ),
    		Arguments.of( new Product( null, "Product 3", false, 60, null ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Product( 1, "Product 1.1", true, 20, null ) ),
    		Arguments.of( new Product( 2, "Product 2", true, 10, null ) ),
    		Arguments.of( new Product( 3, "Product 3.3", false, 60, null ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
		Product mock = new Product( 1, "Product 1", true, 10, null );		
		
	    return Stream.of(
    		Arguments.of( mock, Product.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, Product.builder().name( mock.getName() ).build(), 1 ),
    		Arguments.of( mock, Product.builder().active( mock.getActive() ).build(), 2 ),
    		Arguments.of( mock, Product.builder().proposalExpirationDays( mock.getProposalExpirationDays() ).build(), 1 )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSearch() {
		Product mock = new Product( 1, "Product 1", true, 10, null );		
		
	    return Stream.of(
    		Arguments.of( mock, Product.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, Product.builder().name( "Product" ).build(), 3 ),
    		Arguments.of( mock, Product.builder().active( mock.getActive() ).build(), 2 ),
    		Arguments.of( mock, Product.builder().proposalExpirationDays( mock.getProposalExpirationDays() ).build(), 1 )
	    );
	}
}
