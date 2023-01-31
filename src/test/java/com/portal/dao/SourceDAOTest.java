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

import com.portal.dao.impl.SourceDAO;
import com.portal.model.Source;

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
class SourceDAOTest {

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
	private SourceDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.SourceDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Source entity ) throws Exception {
		Optional<Source> entityId = dao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "listAll - Quando listar todos as fontes" )
	void whenListAll_ReturnListSource() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "src_id");
		List<Source> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	@DisplayName( "getById - Dado um ID existente retornar a fonte" )
	void givenExistedSource_whenGetById_ThenReturnSource() throws Exception {
		
		Source mock = Source.builder()
								.id( 1 )
								.name( "Source 1" )
								.active( true )
								.build();

		Optional<Source> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getName(), entityDB.get().getName());
		assertEquals( mock.getActive(), entityDB.get().getActive());
	}
	
	@Test
	@Order(2)
	@DisplayName( "find - Dado um ID existente retornar a fonte" )
	void givenExistedSourceId_whenFind_ThenReturnSource() throws Exception {
		
		Source mock = Source.builder()
								.id( 1 )
								.name( "Source 1" )
								.active( true )
								.build();

		List<Source> entityDB = dao.find( Source.builder().id(1).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock, entityDB.get(0) );
		assertEquals( mock.getName(), entityDB.get(0).getName());
		assertEquals( mock.getActive(), entityDB.get(0).getActive());
	}
	
	@Test
	@Order(2)
	@DisplayName( "find - Dado um nome existente retornar a fonte" )
	void givenExistedSourceName_whenFind_ThenReturnSource() throws Exception {
		
		Source mock = Source.builder()
								.id( 1 )
								.name( "Source 1" )
								.active( true )
								.build();

		List<Source> entityDB = dao.find( Source.builder().name( mock.getName() ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock, entityDB.get(0) );
		assertEquals( mock.getName(), entityDB.get(0).getName());
		assertEquals( mock.getActive(), entityDB.get(0).getActive());
	}
	
	@Test
	@Order(2)
	@DisplayName( "find - Dado uma flag retornar a fonte" )
	void givenExistedSourceActive_whenFind_ThenReturnSource() throws Exception {
		
		Source mock = Source.builder()
									.id( 1 )
									.name( "Source 1" )
									.active( true )
									.build();

		List<Source> entityDB = dao.find( Source.builder().active( mock.getActive() ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
		
		Optional<Source> entity = entityDB.stream().filter(item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entity );
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getName(), entity.get().getName());
		assertEquals( mock.getActive(), entity.get().getActive());
	}
	
	@Test
	@Order(2)
	@DisplayName( "search - Dado um ID existente retornar a fonte" )
	void givenExistedSourceId_whenSearch_ThenReturnSource() throws Exception {
		
		Source mock = Source.builder()
								.id( 1 )
								.name( "Source 1" )
								.active( true )
								.build();

		List<Source> entityDB = dao.search( Source.builder().id(1).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock, entityDB.get(0) );
		assertEquals( mock.getName(), entityDB.get(0).getName());
		assertEquals( mock.getActive(), entityDB.get(0).getActive());
	}
	
	@Test
	@Order(2)
	@DisplayName( "search - Dado um nome existente retornar a fonte" )
	void givenExistedSourceName_whenSearch_ThenReturnSource() throws Exception {
		
		Source mock = Source.builder()
								.id( 1 )
								.name( "Source 1" )
								.active( true )
								.build();

		List<Source> entityDB = dao.search( Source.builder().name( "Source" ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 3, entityDB.size() );
		
		Optional<Source> entity = entityDB.stream().filter(item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entity );
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getName(), entity.get().getName());
		assertEquals( mock.getActive(), entity.get().getActive());		
	}
	
	@Test
	@Order(2)
	@DisplayName( "search - Dado uma flag retornar a fonte" )
	void givenExistedSourceActive_whenSearch_ThenReturnSource() throws Exception {
		
		Source mock = Source.builder()
									.id( 1 )
									.name( "Source 1" )
									.active( true )
									.build();

		List<Source> entityDB = dao.search( Source.builder().active( mock.getActive() ).build(), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
		
		Optional<Source> entity = entityDB.stream().filter(item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entity );
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getName(), entity.get().getName());
		assertEquals( mock.getActive(), entity.get().getActive());
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.SourceDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( Source mock ) throws Exception {
		dao.update( mock );
		
		Optional<Source> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getName(), entityDB.get().getName());
		assertEquals( mock.getActive(), entityDB.get().getActive());
	}
	
	@Test
	@Order(4)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedSource_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Source> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Source( null, "Source 1", true ) ),
    		Arguments.of( new Source( null, "Source 2", true ) ),
    		Arguments.of( new Source( null, "Source 3", false ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Source( 1, "Source 1.1", true ) ),
    		Arguments.of( new Source( 2, "Source 2", true ) ),
    		Arguments.of( new Source( 3, "Source 3.3", false ) )
	    );
	}
	
}
