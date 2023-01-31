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

import com.portal.dao.impl.ItemTypeDAO;
import com.portal.model.ItemType;

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
class ItemTypeDAOTest {

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
	private ItemTypeDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ItemTypeDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( ItemType entity ) throws Exception {
		Optional<ItemType> entityId = dao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "listAll - Quando listar todos os tipos de item" )
	void whenListAll_ReturnListItemType() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "itt_id");
		List<ItemType> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	@DisplayName( "getById - Dado um ID existente retornar o tipo de item" )
	void givenExistedItemType_whenGetById_ThenReturnItemType() throws Exception {
		
		ItemType entity = new ItemType( 1, "ItemType 1", true, false, 0);

		Optional<ItemType> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity.getId() , entityDB.get().getId());
		assertEquals( entity.getName() , entityDB.get().getName());
		assertEquals( entity.getMandatory() , entityDB.get().getMandatory());
		assertEquals( entity.getMulti() , entityDB.get().getMulti());
		assertEquals( entity.getSeq() , entityDB.get().getSeq());
	}

	@Test
	@Order(2)
	@DisplayName( "find - Dado um ID existente retornar o tipo de item" )
	void givenExistedItemTypeId_whenFind_ThenReturnItemType() throws Exception {
		
		ItemType entity = new ItemType( 1, "ItemType 1", true, false, 0);

		List<ItemType> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( entity.getId() , entityDB.get(0).getId());
		assertEquals( entity.getName() , entityDB.get(0).getName());
		assertEquals( entity.getMandatory() , entityDB.get(0).getMandatory());
		assertEquals( entity.getMulti() , entityDB.get(0).getMulti());
		assertEquals( entity.getSeq() , entityDB.get(0).getSeq());
	}
	

	@Test
	@Order(2)
	@DisplayName( "find - Dado um nome existente retornar o tipo de item" )
	void givenExistedItemTypeName_whenFind_ThenReturnItemType() throws Exception {
		
		ItemType entity = ItemType.builder()
								.name( "ItemType 1" )
								.build();

		List<ItemType> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "find - Dado uma flag mandatório retornar o tipo de item" )
	void givenExistedItemTypeMandatory_whenFind_ThenReturnItemType() throws Exception {
		
		ItemType entity = ItemType.builder()
								.mandatory( true )
								.build();

		List<ItemType> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "find - Dado uma flag multi retornar o tipo de item" )
	void givenExistedItemTypeMulti_whenFind_ThenReturnItemType() throws Exception {
		
		ItemType entity = ItemType.builder()
								.multi( true )
								.build();

		List<ItemType> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "find - Dado uma sequencia retornar o tipo de item" )
	void givenExistedItemTypeSeq_whenFind_ThenReturnItemType() throws Exception {
		
		ItemType entity = ItemType.builder()
								.seq( 0 )
								.build();

		List<ItemType> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "search - Dado um ID existente retornar o tipo de item" )
	void givenExistedItemTypeId_whenSearch_ThenReturnItemType() throws Exception {
		
		ItemType entity = ItemType.builder()
									.id( 3 )
									.build();

		List<ItemType> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( entityDB.get(0).getId(), entity.getId() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "search - Dado um nome existente retornar o tipo de item" )
	void givenExistedItemTypeName_whenSearch_ThenReturnItemType() throws Exception {
		
		ItemType entity = ItemType.builder()
								.name( "ItemType" )
								.build();

		List<ItemType> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 3, entityDB.size() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "search - Dado uma flag mandatório retornar o tipo de item" )
	void givenExistedItemTypeMandatory_whenSearch_ThenReturnItemType() throws Exception {
		ItemType entity = ItemType.builder()
								.mandatory( false )
								.build();

		List<ItemType> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "search - Dado uma flag multi retornar o tipo de item" )
	void givenExistedItemTypeMulti_whenSearch_ThenReturnItemType() throws Exception {
		ItemType entity = ItemType.builder()
								.multi( false )
								.build();

		List<ItemType> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
	}
	

	@Test
	@Order(2)
	@DisplayName( "search - Dado uma sequencia retornar o tipo de item" )
	void givenExistedItemTypeSeq_whenSearch_ThenReturnItemType() throws Exception {
		ItemType entity = ItemType.builder()
								.seq( 1 )
								.build();

		List<ItemType> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
	}
	

	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ItemTypeDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( ItemType entity ) throws Exception {
		dao.update( entity );
		
		Optional<ItemType> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
	}
	
	@Test
	@Order(4)
	@DisplayName( "hasItemRelationship - Quando não existe relacionamento com item retorna false" )
	void givenItemType_whenCheckHasItemRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasItemRelationship(1);
		assertFalse( db );
	}

	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedItemType_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<ItemType> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new ItemType( null, "ItemType 1", true, false, 0 ) ),
    		Arguments.of( new ItemType( null, "ItemType 2", true, false, 0 ) ),
    		Arguments.of( new ItemType( null, "ItemType 3", true, false, 0 ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new ItemType( 1, "ItemType 1.1", true, false, 1 ) ),
    		Arguments.of( new ItemType( 2, "ItemType 2", true, false, 0) ),
    		Arguments.of( new ItemType( 3, "ItemType 3.3", true, false, 1 ) )
	    );
	}
	
}
