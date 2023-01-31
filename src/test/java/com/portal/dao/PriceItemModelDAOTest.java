package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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

import com.portal.dao.impl.PriceItemModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.ItemType;
import com.portal.model.Model;
import com.portal.model.PriceItemModel;
import com.portal.model.PriceList;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ItemModelService;
import com.portal.service.imp.ItemService;
import com.portal.service.imp.ItemTypeService;
import com.portal.service.imp.ModelService;
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
class PriceItemModelDAOTest {

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
	private PriceItemModelDAO dao;

	@Autowired
	private ItemService itemService;
	
	@Autowired
	private ItemTypeService itemTypeService;
	
	@Autowired
	private PriceListService priceListService;
	
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private ClassifierService classifierService;
	
	@Autowired
	private ModelService modelService;
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private ItemModelService itemModelService;
	
	private static final ItemType itemType = new ItemType(null, "ItemType 1", true, false, 1);
	private static final Item itemMock = new Item(null, "Item 1", "200", 1, false, false, new Classifier(23), itemType, null, null, null, null, null, null, null, null, null);
	private static final Brand brandMock = new Brand(null, "BRAND 1", true );
	private static final Model modelMock = new Model(null, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static final ItemModel itemModelMock1 = new ItemModel(null, 2000, 2015, itemMock, modelMock);
	private static final ItemModel itemModelMock2 = new ItemModel(null, 2016, 2017, itemMock, modelMock);
	private static final Channel channelMock = new Channel(null, "Channel 1", true, true, true);
	private static final PriceList priceListMock1 = new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	private static final PriceList priceListMock2 = new PriceList(null, "PriceList 2", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	
	/**
	 * Devido a injeção de dependências não é possivil usar o @BerforeAll
	 * Por esse motivo forçamos ser o primeiro passo do teste a inserção dos dados 
	 * usados como base.
	 */
	@Test
	@Order(1)
	void setup() throws Exception {
		UserProfileDTO profile = new UserProfileDTO( new UserModel( "MOCK USER" ) );
		
		for( ModelBodyType classifiers : ModelBodyType.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}

		for( ModelCategory classifiers : ModelCategory.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}
		
		for( ModelSize classifiers : ModelSize.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}
		
		Optional<ItemType> dbItemType = itemTypeService.save(itemType, profile);
		itemType.setId( dbItemType.get().getId() );
		Optional<Item> dbItem = itemService.save( itemMock, profile);
		itemMock.setId( dbItem.get().getId() );
		Optional<Brand> dbBrand = brandService.save(brandMock, profile);
		brandMock.setId( dbBrand.get().getId() );
		Optional<Model> dbModel = modelService.save(modelMock, profile);
		modelMock.setId( dbModel.get().getId() );
		Optional<ItemModel> dbItemModel = itemModelService.save(itemModelMock1, profile);
		itemModelMock1.setId( dbItemModel.get().getId() );
		Optional<ItemModel> dbItemModel2 = itemModelService.save(itemModelMock2, profile);
		itemModelMock2.setId( dbItemModel2.get().getId() );
		Optional<Channel> dbChannel = channelService.save( channelMock, profile);
		channelMock.setId( dbChannel.get().getId() );
		Optional<PriceList> dbPriceList = priceListService.save( priceListMock1, profile);
		priceListMock1.setId( dbPriceList.get().getId() );
		Optional<PriceList> dbPriceList2 = priceListService.save( priceListMock2, profile);
		priceListMock2.setId( dbPriceList2.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceItemModelDAOTest#listEntityToSave")
	@DisplayName( "save - Quando salva o relacionamento então não retorna erro" )
	void whenSave_ThenNoError( PriceItemModel model ) throws Exception {
		assertDoesNotThrow(()->dao.save( model ));
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceItemModelDAOTest#listEntityToGetById")
	@DisplayName( "getById - Dado um registro existente quando buscamos por ID deve retornar a entidade relacionada" )
	void givenExistedItemModelPrice_whenGetById_ThenReturn( PriceItemModel mock ) throws Exception {
		Optional<PriceItemModel> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		
		if( mock.getId().equals( 1 ) || mock.getId().equals( 2 ) ) {
			assertNotNull( entityDB.get().getItemModel() );
			assertEquals( mock.getItemModel(), entityDB.get().getItemModel() );
		} else {
			assertNull( entityDB.get().getItemModel() );
		}
		
		if( mock.getId().equals( 3 ) ) {
			assertNotNull( entityDB.get().getBrand() );
			assertEquals( mock.getBrand(), entityDB.get().getBrand() );
		} else {
			assertNull( entityDB.get().getBrand() );
		}
		
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceItemModelDAOTest#listEntityToGetById")
	@DisplayName( "find - Dado o ID do relacionamento quando procurado retornar a entidade" )
	void givenExistedItemModelPriceId_whenFind_ThenReturnEntity( PriceItemModel mock ) throws Exception {
		PriceItemModel findEntity = PriceItemModel.builder()
											.id( mock.getId() )
											.build();

		List<PriceItemModel> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		PriceItemModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getPrice(), entityDB.getPrice() );
		assertEquals( mock.getAllBrands(), entityDB.getAllBrands() );
		assertNotNull( entityDB.getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.getPriceList() );
		
		if( mock.getId().equals( 1 ) || mock.getId().equals( 2 ) ) {
			assertNotNull( entityDB.getItemModel() );
			assertEquals( mock.getItemModel(), entityDB.getItemModel() );
		} else {
			assertNull( entityDB.getItemModel() );
		}
		
		if( mock.getId().equals( 3 ) ) {
			assertNotNull( entityDB.getBrand() );
			assertEquals( mock.getBrand(), entityDB.getBrand() );
		} else {
			assertNull( entityDB.getBrand() );
		}
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID da lista de preço quando procurado retornar a entidade" )
	void givenExistedPriceList_whenFind_ThenReturnEntity() throws Exception {
		PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock, itemModelMock1, null);

		PriceItemModel findEntity = PriceItemModel.builder()
												.priceList( priceListMock1 )
												.build();

		List<PriceItemModel> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 4, listDB.size() );

		Optional<PriceItemModel> entityDB = listDB.stream().filter( item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertEquals( mock.getAllBrands(), entityDB.get().getAllBrands() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getItemModel() );
		assertEquals( mock.getItemModel(), entityDB.get().getItemModel() );
		assertNull( entityDB.get().getBrand());
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID do item model quando procurado retornar a entidade" )
	void givenExistedItemModel_whenFind_ThenReturnEntity() throws Exception {
		PriceItemModel mock = new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock, itemModelMock1, null);

		PriceItemModel findEntity = PriceItemModel.builder()
													.itemModel( mock.getItemModel() )
													.build();

		List<PriceItemModel> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		PriceItemModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getPrice(), entityDB.getPrice() );
		assertEquals( mock.getAllBrands(), entityDB.getAllBrands() );
		assertNotNull( entityDB.getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.getPriceList() );
		assertNotNull( entityDB.getItemModel() );
		assertEquals( mock.getItemModel(), entityDB.getItemModel() );
		assertNull( entityDB.getBrand() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceItemModelDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( PriceItemModel entity, int control ) throws Exception {
		dao.update( entity );
		
		Optional<PriceItemModel> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getPrice(), entityDB.get().getPrice() );
		assertEquals( entity.getAllBrands(), entityDB.get().getAllBrands() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( entity.getPriceList(), entityDB.get().getPriceList() );
		
		if( control == 1 ) {
			assertNotNull( entityDB.get().getItemModel() );
			assertEquals( entity.getItemModel(), entityDB.get().getItemModel() );
		} else {
			assertNull( entityDB.get().getItemModel() );
		}
		
		if( control == 2 ) {
			assertNotNull( entityDB.get().getBrand() );
			assertEquals( entity.getBrand(), entityDB.get().getBrand() );
		} else {
			assertNull( entityDB.get().getBrand() );
		}
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedItemModelPrice_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		dao.delete( id );
		Optional<PriceItemModel> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new PriceItemModel(null, 100d, false, false , priceListMock1, itemMock, itemModelMock1, null) ),
    		Arguments.of( new PriceItemModel(null, 150d, false, false , priceListMock1, itemMock, itemModelMock2, null) ),
    		Arguments.of( new PriceItemModel(null, 200d, true, false , priceListMock1, itemMock, null, brandMock) ),
    		Arguments.of( new PriceItemModel(null, 300d, false, true, priceListMock1, itemMock, null, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToGetById() {
	    return Stream.of(
    		Arguments.of( new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock, itemModelMock1, null) ),
    		Arguments.of( new PriceItemModel(2, 150d, false, false , priceListMock1, itemMock, itemModelMock2, null) ),
    		Arguments.of( new PriceItemModel(3, 200d, true, false , priceListMock1, itemMock, null, brandMock) ),
    		Arguments.of( new PriceItemModel(4, 300d, false, true, priceListMock1, itemMock, null, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new PriceItemModel(1, 100d, false, false , priceListMock1, itemMock, itemModelMock1, null),1 ),
    		Arguments.of( new PriceItemModel(1, 200d, true, false , priceListMock1, itemMock, null, brandMock),2 ),
    		Arguments.of( new PriceItemModel(1, 300d, false, true, priceListMock1, itemMock,  null, null),3 )
	    );
	}
}
