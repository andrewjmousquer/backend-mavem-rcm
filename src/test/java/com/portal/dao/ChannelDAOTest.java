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

import com.portal.dao.impl.ChannelDAO;
import com.portal.model.Channel;

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
class ChannelDAOTest {

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
	private ChannelDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ChannelDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Channel entity ) throws Exception {
		Optional<Channel> entityId = dao.save( entity );
		
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
		assertEquals( entity.getName(), entityId.get().getName( ));
		assertEquals( entity.getActive(), entityId.get().getActive( ));
		assertEquals( entity.getHasPartner(), entityId.get().getHasPartner( ));
		assertEquals( entity.getHasInternalSale(), entityId.get().getHasInternalSale( ));
	}
	
	@Test
	@Order(2)
	@DisplayName( "listAll - Quando listar todos os canais" )
	void whenListAll_ReturnListChannel() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "chn_id");
		List<Channel> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	@DisplayName( "getById - Dado um ID existente retornar o canal" )
	void givenExistedChannel_whenGetById_ThenReturnChannel() throws Exception {
		
		Channel entity = new Channel( 1, "Channel 1", true, true, false );

		Optional<Channel> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getName(), entityDB.get().getName( ));
		assertEquals( entity.getActive(), entityDB.get().getActive( ));
		assertEquals( entity.getHasPartner(), entityDB.get().getHasPartner( ));
		assertEquals( entity.getHasInternalSale(), entityDB.get().getHasInternalSale( ));
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ChannelDAOTest#listEntityToFind")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenFind_ReturnProposal( Channel mock, Channel filter, int expectedSize ) throws Exception {
		List<Channel> entityDB = dao.find( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<Channel> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertEquals( mock, entity.get() );
		assertEquals( mock.getId(), entity.get().getId() );
		assertEquals( mock.getName(), entity.get().getName( ));
		assertEquals( mock.getActive(), entity.get().getActive( ));
		assertEquals( mock.getHasPartner(), entity.get().getHasPartner( ));
		assertEquals( mock.getHasInternalSale(), entity.get().getHasInternalSale( ));
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ChannelDAOTest#listEntityToSearch")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenSearch_ReturnProposal( Channel mock, Channel filter, int expectedSize ) throws Exception {
		List<Channel> entityDB = dao.search( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<Channel> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertEquals( mock, entity.get() );
		assertEquals( mock.getId(), entity.get().getId() );
		assertEquals( mock.getName(), entity.get().getName( ));
		assertEquals( mock.getActive(), entity.get().getActive( ));
		assertEquals( mock.getHasPartner(), entity.get().getHasPartner( ));
		assertEquals( mock.getHasInternalSale(), entity.get().getHasInternalSale( ));
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ChannelDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( Channel entity ) throws Exception {
		dao.update( entity );
		
		Optional<Channel> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getName(), entityDB.get().getName( ));
		assertEquals( entity.getActive(), entityDB.get().getActive( ));
		assertEquals( entity.getHasPartner(), entityDB.get().getHasPartner( ));
		assertEquals( entity.getHasInternalSale(), entityDB.get().getHasInternalSale( ));
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedChannel_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Channel> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Channel( null, "Channel 1", true, true, false ) ),
    		Arguments.of( new Channel( null, "Channel 2", true, true, true ) ),
    		Arguments.of( new Channel( null, "Channel 3", false, true, false ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Channel( 1, "Channel 1.1", true, false, true ) ),
    		Arguments.of( new Channel( 2, "Channel 2", true, true, false ) ),
    		Arguments.of( new Channel( 3, "Channel 3.3", true, true, false ) )
	    );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
		Channel mock = new Channel( 1, "Channel 1", true, true, false );		
		
	    return Stream.of(
    		Arguments.of( mock, Channel.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, Channel.builder().name( mock.getName() ).build(), 1 ),
    		Arguments.of( mock, Channel.builder().active( mock.getActive() ).build(), 2 ),
    		Arguments.of( mock, Channel.builder().hasPartner( mock.getHasPartner() ).build(), 3 ),
    		Arguments.of( mock, Channel.builder().hasInternalSale( mock.getHasInternalSale() ).build(), 2 )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSearch() {
		Channel mock = new Channel( 1, "Channel 1", true, true, false );		
		
	    return Stream.of(
    		Arguments.of( mock, Channel.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, Channel.builder().name( "Channel" ).build(), 3 ),
    		Arguments.of( mock, Channel.builder().active( mock.getActive() ).build(), 2 ),
    		Arguments.of( mock, Channel.builder().hasPartner( mock.getHasPartner() ).build(), 3 ),
    		Arguments.of( mock, Channel.builder().hasInternalSale( mock.getHasInternalSale() ).build(), 2 )
	    );
	}
}
