package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dao.impl.PriceItemDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemType;
import com.portal.model.PriceItem;
import com.portal.model.PriceList;
import com.portal.model.UserModel;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ItemService;
import com.portal.service.imp.ItemTypeService;
import com.portal.service.imp.PriceListService;

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
class PriceItemDAOTest {

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
	private PriceItemDAO dao;

	@Autowired
	private ItemService itemService;
	
	@Autowired
	private ItemTypeService itemTypeService;
	
	@Autowired
	private PriceListService priceListService;
	
	@Autowired
	private ChannelService channelService;
	
	private static final ItemType itemType = new ItemType(null, "ItemType 1", true, false, 1);
	private static final Item itemMock1 = new Item(null, "Item 1", "200", 1, false, false, new Classifier(23), itemType, null, null, null, null, null, null, null, null, null);
	private static final Item itemMock2 = new Item(null, "Item 2", "300", 1, false, false, new Classifier(23), itemType, null, null, null, null, null, null, null, null, null);

	private static Channel channelMock = new Channel(null, "Channel 1", true, true, true);
	private static PriceList priceListMock1 = new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	private static PriceList priceListMock2 = new PriceList(null, "PriceList 2", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	
	/**
	 * Devido a injeção de dependências não é possivil usar o @BerforeAll
	 * Por esse motivo forçamos ser o primeiro passo do teste a inserção dos dados 
	 * usados como base.
	 */
	@Test
	@Order(1)
	void setup() throws Exception {
		UserProfileDTO profile = new UserProfileDTO( new UserModel( "MOCK USER" ) );
		
		Optional<Channel> dbChannel = channelService.save( channelMock, profile);
		channelMock.setId( dbChannel.get().getId() );
		Optional<ItemType> dbItemType = itemTypeService.save( itemType, profile);
		itemType.setId( dbItemType.get().getId() );
		Optional<PriceList> dbPriceList = priceListService.save( priceListMock1, profile);
		priceListMock1.setId( dbPriceList.get().getId() );
		Optional<PriceList> dbPriceList2 = priceListService.save( priceListMock2, profile);
		priceListMock2.setId( dbPriceList2.get().getId() );
		Optional<Item> dbItem = itemService.save( itemMock1, profile);
		itemMock1.setId( dbItem.get().getId() );
		Optional<Item> dbItem2 = itemService.save( itemMock2, profile);
		itemMock2.setId( dbItem2.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceItemDAOTest#listEntityToSave")
	@DisplayName( "save - Quando salva o relacionamento então não retorna erro" )
	void whenSave_ThenNoError( PriceItem model ) throws Exception {
		assertDoesNotThrow(()->dao.save( model ));
	}
	
	@Test
	@Order(3)
	void givenExistedItemPrice_whenGetById_ThenReturn() throws Exception {
		PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);

		Optional<PriceItem> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getItem() );
		assertEquals( mock.getItem(), entityDB.get().getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID do relacionamento quando procurado retornar a entidade" )
	void givenExistedItemPriceId_whenFind_ThenReturnEntity() throws Exception {
		PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);

		PriceItem findEntity = PriceItem.builder()
											.id( mock.getId() )
											.build();

		List<PriceItem> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		PriceItem entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getPrice(), entityDB.getPrice() );
		assertNotNull( entityDB.getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.getPriceList() );
		assertNotNull( entityDB.getItem() );
		assertEquals( mock.getItem(), entityDB.getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID da lista de preço quando procurado retornar a entidade" )
	void givenExistedPriceList_whenFind_ThenReturnEntity() throws Exception {
		PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);

		PriceItem findEntity = PriceItem.builder()
												.priceList( priceListMock1 )
												.build();

		List<PriceItem> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 2, listDB.size() );

		Optional<PriceItem> entityDB = listDB.stream().filter( item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getItem() );
		assertEquals( mock.getItem(), entityDB.get().getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID do item quando procurado retornar a entidade" )
	void givenExistedItem_whenFind_ThenReturnEntity() throws Exception {
		PriceItem mock = new PriceItem(1, 100d, itemMock1, priceListMock1);

		PriceItem findEntity = PriceItem.builder()
												.item(itemMock1)
												.build();

		List<PriceItem> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		Optional<PriceItem> entityDB = listDB.stream().filter( item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getItem() );
		assertEquals( mock.getItem(), entityDB.get().getItem() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceItemDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( PriceItem entity ) throws Exception {
		dao.update( entity );
		
		Optional<PriceItem> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( entity.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getItem() );
		assertEquals( entity.getItem(), entityDB.get().getItem() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedItemPrice_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		dao.delete( id );
		Optional<PriceItem> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new PriceItem(null, 100d, itemMock1, priceListMock1) ),
			Arguments.of( new PriceItem(null, 200d, itemMock2, priceListMock1) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new PriceItem(1, 100d, itemMock1, priceListMock1) ),
			Arguments.of( new PriceItem(2, 200d, itemMock2, priceListMock1) )
	    );
	}
}
