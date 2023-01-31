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

import com.portal.dao.impl.PartnerGroupDAO;
import com.portal.model.PartnerGroup;

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
class PartnerGroupDAOTest {

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
	private PartnerGroupDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerGroupDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( PartnerGroup model ) throws Exception {
		Optional<PartnerGroup> modelId = dao.save( model );
		assertNotNull( modelId );
		assertTrue( modelId.isPresent() );
		assertEquals( model.getId(), modelId.get().getId() );
	}
	
	@Test
	@Order(2)
	@DisplayName( "listAll - Quando listar todos os grupos retornar a lista completa" )
	void whenListAll_ReturnListModel() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "ptg_id");
		List<PartnerGroup> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	@DisplayName( "getById - Dado um ID existente retornar o grupo" )
	void givenExistedPartnerGroup_whenGetById_ThenReturnModel() throws Exception {
		
		PartnerGroup mock = new PartnerGroup( 1, "PartnerGroup 1", true );

		Optional<PartnerGroup> modelDB = dao.getById( mock.getId() );
		
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( mock, modelDB.get() );
		assertEquals( mock.getName(), modelDB.get().getName() );
		assertEquals( mock.getActive(), modelDB.get().getActive() );
	}

	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerGroupDAOTest#listEntityToFind")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenFind_ReturnProposal( PartnerGroup mock, PartnerGroup filter, int expectedSize ) throws Exception {
		List<PartnerGroup> entityDB = dao.find( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<PartnerGroup> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getName(), entity.get().getName() );
		assertEquals( mock.getActive(), entity.get().getActive() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerGroupDAOTest#listEntityToSearch")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenSearch_ReturnProposal( PartnerGroup mock, PartnerGroup filter, int expectedSize ) throws Exception {
		List<PartnerGroup> entityDB = dao.search( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<PartnerGroup> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getName(), entity.get().getName() );
		assertEquals( mock.getActive(), entity.get().getActive() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerGroupDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( PartnerGroup model ) throws Exception {
		dao.update( model );
		
		Optional<PartnerGroup> entityDB = dao.getById( model.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( model, entityDB.get() );
		assertEquals( model.getName(), entityDB.get().getName() );
		assertEquals( model.getActive(), entityDB.get().getActive() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedPartnerGroup_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<PartnerGroup> modelDB = dao.getById( id );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new PartnerGroup( null, "PartnerGroup 1", true ) ),
    		Arguments.of( new PartnerGroup( null, "PartnerGroup 2", true ) ),
    		Arguments.of( new PartnerGroup( null, "PartnerGroup 3", true ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new PartnerGroup( 1, "PartnerGroup 1.1", true ) ),
    		Arguments.of( new PartnerGroup( 2, "PartnerGroup 2", false ) ),
    		Arguments.of( new PartnerGroup( 3, "PartnerGroup 3.3", true ) )
	    );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
		PartnerGroup mock = new PartnerGroup( 1, "PartnerGroup 1", true );		
		
	    return Stream.of(
    		Arguments.of( mock, PartnerGroup.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, PartnerGroup.builder().name( mock.getName() ).build(), 1 ),
    		Arguments.of( mock, PartnerGroup.builder().active( mock.getActive() ).build(), 3 )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSearch() {
		PartnerGroup mock = new PartnerGroup( 1, "PartnerGroup 1", true );		
		
	    return Stream.of(
    		Arguments.of( mock, PartnerGroup.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, PartnerGroup.builder().name( "Partner" ).build(), 3 ),
    		Arguments.of( mock, PartnerGroup.builder().active( mock.getActive() ).build(), 3 )
	    );
	}
	
}


