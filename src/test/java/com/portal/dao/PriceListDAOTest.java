package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
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

import com.portal.dao.impl.PriceListDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.model.Channel;
import com.portal.model.PriceList;
import com.portal.model.UserModel;
import com.portal.service.imp.ChannelService;

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
class PriceListDAOTest {

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
	private PriceListDAO dao;
	
	@Autowired
	private ChannelService channelService;
	
	private static final Channel channel = new Channel(null, "Channel 1", true, true, true);
	private static final Channel channel2 = new Channel(null, "Channel 2", true, true, true);
	
	/**
	 * Devido a injeção de dependências não é possivil usar o @BerforeAll
	 * Por esse motivo forçamos ser o primeiro passo do teste a inserção dos dados 
	 * usados como base.
	 */
	@Test
	@Order(1)
	void setup() throws Exception {
		UserProfileDTO profile = new UserProfileDTO( new UserModel( "MOCK USER" ) );
		
		Optional<Channel> dbChannel = channelService.save(channel, profile);
		channel.setId( dbChannel.get().getId() );
		Optional<Channel> dbChannel2 = channelService.save(channel2, profile);
		channel2.setId( dbChannel2.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceListDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( PriceList entity ) throws Exception {
		Optional<PriceList> entityId = dao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "listAll - Quando listar todos as listas de preços" )
	void whenListAll_ReturnListPriceList() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "prl_id");
		List<PriceList> list = dao.listAll( pageReq  );

		assertEquals( listEntityToSave().count(), list.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar a lista de preço" )
	void givenExistedPriceList_whenGetById_ThenReturnPriceList() throws Exception {
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);

		Optional<PriceList> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( entityDB.get().getName(), mock.getName());
		assertEquals( entityDB.get().getStart(), mock.getStart());
		assertEquals( entityDB.get().getEnd(), mock.getEnd());
		assertNotNull( entityDB.get().getChannel() );
		assertEquals( entityDB.get().getChannel(), mock.getChannel());
		assertEquals( entityDB.get().getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get().getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um ID existente retornar a lista de preço" )
	void givenExistedPriceListId_whenFind_ThenReturnPriceList() throws Exception {
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);

		PriceList entity = PriceList.builder()
								.id( 1 )
								.build();

		List<PriceList> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um nome existente retornar a lista de preço" )
	void givenExistedPriceListName_whenFind_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
		
		PriceList entity = PriceList.builder()
								.name( "PriceList 1" )
								.build();

		List<PriceList> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado uma canal retornar a lista de preço" )
	void givenExistedPriceListChannel_whenFind_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
		
		PriceList entity = PriceList.builder()
								.channel(channel)
								.build();

		List<PriceList> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado a data de início retornar a lista de preço" )
	void givenExistedPriceListStartDate_whenFind_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(2, "PriceList 2", LocalDateTime.of(2022, 01, 01, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 31, 00, 00, 00, 00), channel2, false);
		
		PriceList entity = PriceList.builder()
								.start( LocalDateTime.of(2022, 01, 01, 00, 00, 00, 00) )
								.build();

		List<PriceList> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado a data de fim retornar a lista de preço" )
	void givenExistedPriceListEndDate_whenFind_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
		
		PriceList entity = PriceList.builder()
								.end( LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00) )
								.build();

		List<PriceList> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado todos os filtros lista de preço" )
	void givenExistedPriceList_whenFind_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(2, "PriceList 2", LocalDateTime.of(2022, 01, 01, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 31, 00, 00, 00, 00), channel2, false);
		
		List<PriceList> entityDB = dao.find( mock, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByStartPeriod - Dado um periodo quando procurado por data de inicio retorna lista" )
	void givenPriceListStartDate_whenFindByStartPeriod_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(3, "PriceList 3", LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 31, 00, 00, 00, 00), channel2, false);
		
		List<PriceList> entityDB = dao.findByStartPeriod( null, LocalDateTime.of(2022, 01, 10, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 20, 00, 00, 00, 00), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByEndPeriod - Dado um periodo quando procurado por data de término retorna lista" )
	void givenPriceListEndDate_whenFindByEndPeriod_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(3, "PriceList 3", LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 31, 00, 00, 00, 00), channel2, false);
		
		List<PriceList> entityDB = dao.findByStartPeriod( null, LocalDateTime.of(2022, 01, 12, 00, 00, 00, 00), LocalDateTime.of(2022, 02, 02, 00, 00, 00, 00), null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um ID existente retornar a lista de preço" )
	void givenPriceListId_whenSearch_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
		
		PriceList entity = PriceList.builder()
									.id( 1 )
									.build();

		List<PriceList> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um nome existente retornar a lista de preço" )
	void givenExistedPriceListName_whenSearch_ThenReturnPriceList() throws Exception {
		
		PriceList entity = PriceList.builder()
								.name( "PriceList" )
								.build();

		List<PriceList> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 3, entityDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um canal retornar a lista de preço" )
	void givenPriceListChannel_whenSearch_ThenReturnPriceList() throws Exception {
		PriceList entity = PriceList.builder()
								.channel(channel2)
								.build();

		List<PriceList> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado a data de início retornar a lista de preço" )
	void givenPriceListStartDate_whenSearch_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(2, "PriceList 2", LocalDateTime.of(2022, 01, 01, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 31, 00, 00, 00, 00), channel2, false);
		
		PriceList entity = PriceList.builder()
								.start( LocalDateTime.of(2022, 01, 01, 00, 00, 00, 00) )
								.build();

		List<PriceList> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado a data de fim retornar a lista de preço" )
	void givenPriceListEndDate_whenSearch_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
		
		PriceList entity = PriceList.builder()
								.end( LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00) )
								.build();

		List<PriceList> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado todos os filtros lista de preço" )
	void givenPriceList_whenSearch_ThenReturnPriceList() throws Exception {
		
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
		
		List<PriceList> entityDB = dao.search( mock, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( entityDB.get(0).getId(), mock.getId() );
		assertEquals( entityDB.get(0).getName(), mock.getName());
		assertEquals( entityDB.get(0).getStart(), mock.getStart());
		assertEquals( entityDB.get(0).getEnd(), mock.getEnd());
		assertNotNull( entityDB.get(0).getChannel() );
		assertEquals( entityDB.get(0).getChannel(), mock.getChannel());
		assertEquals( entityDB.get(0).getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get(0).getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceListDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( PriceList entity ) throws Exception {
		dao.update( entity );
		
		Optional<PriceList> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entityDB.get().getName(), entity.getName());
		assertEquals( entityDB.get().getStart(), entity.getStart());
		assertEquals( entityDB.get().getEnd(), entity.getEnd());
		assertNotNull( entityDB.get().getChannel() );
		assertEquals( entityDB.get().getChannel(), entity.getChannel());
		assertEquals( entityDB.get().getChannel().getName(), entity.getChannel().getName());
		assertEquals( entityDB.get().getChannel().getActive(), entity.getChannel().getActive());
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedPriceList_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<PriceList> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false) ),
    		Arguments.of( new PriceList(null, "PriceList 2", LocalDateTime.of(2022, 01, 01, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 31, 00, 00, 00, 00), channel2, false) ),
    		Arguments.of( new PriceList(null, "PriceList 3", LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 31, 00, 00, 00, 00), channel2, false) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false) ),
    		Arguments.of( new PriceList(2, "PriceList 2", LocalDateTime.of(2022, 01, 01, 00, 00, 00, 00), LocalDateTime.of(2022, 01, 15, 00, 00, 00, 00), channel2, false) ),
    		Arguments.of( new PriceList(3, "PriceList 3", LocalDateTime.of(2022, 02, 15, 00, 00, 00, 00), LocalDateTime.of(2022, 02, 16, 00, 00, 00, 00), channel, false) )
	    );
	}
	
}
