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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dao.impl.ItemModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.ItemType;
import com.portal.model.Model;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ItemService;
import com.portal.service.imp.ItemTypeService;
import com.portal.service.imp.ModelService;

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
class ItemModelDAOTest {

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
	private ItemModelDAO dao;
	
	@Autowired
	private ModelService modelService;
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private ItemTypeService itemTypeService;
	
	@Autowired
	private ItemService itemService;
	
	@Autowired
	private ClassifierService classifierService;
	
	private static Brand brandMock = new Brand( null, "BRAND 1", true );
	private static Model modelMock1 = new Model( null, "MODEL 1", true, brandMock, null, ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static Model modelMock2 = new Model( null, "MODEL 2", true, brandMock, null, ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static ItemType itemTypeMock = new ItemType( null, "ItemType 1", true, false, 1 );
	private static Item itemMock1 = new Item( null, "Item 1", "200", 1, false, false, new Classifier(23), itemTypeMock, null, null, null, null, null, null,  null, null, null);
	private static Item itemMock2 = new Item( null, "Item 2", "300", 1, false, false, new Classifier(23), itemTypeMock, null, null, null, null, null, null, null, null, null);
	
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
		
		Optional<Brand> dbBrand = brandService.save(brandMock, profile);
		brandMock.setId( dbBrand.get().getId() );
		Optional<Model> dbModel = modelService.save(modelMock1, profile);
		modelMock1.setId( dbModel.get().getId() );
		Optional<Model> dbModel2 = modelService.save(modelMock2, profile);
		modelMock2.setId( dbModel2.get().getId() );
		Optional<ItemType> dbItemType = itemTypeService.save(itemTypeMock, profile);
		itemTypeMock.setId( dbItemType.get().getId() );
		Optional<Item> dbItem = itemService.save(itemMock1, profile);
		itemMock1.setId( dbItem.get().getId() );
		Optional<Item> dbItem2 = itemService.save(itemMock2, profile);
		itemMock2.setId( dbItem2.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ItemModelDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( ItemModel entity ) throws Exception {
		Optional<ItemModel> entityId = dao.save( entity );
		
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
		
		Optional<ItemModel> entityDB = dao.getById( entityId.get().getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity.getModelYearStart(), entityDB.get().getModelYearStart());
		assertEquals( entity.getModelYearEnd(), entityDB.get().getModelYearEnd());
		assertNotNull( entityDB.get().getModel() );
		assertEquals( entity.getModel(), entityDB.get().getModel() );
		assertNotNull( entityDB.get().getItem() );
		assertEquals( entity.getItem(), entityDB.get().getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar o relacionamento de produto e modelo" )
	void givenExistedItemModel_whenGetById_ThenReturnItemModel() throws Exception {
		ItemModel mock = new ItemModel(1, 2000, 2005, itemMock1, modelMock1);

		Optional<ItemModel> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getModelYearStart(), entityDB.get().getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.get().getModelYearEnd());
		assertNotNull( entityDB.get().getModel() );
		assertEquals( mock.getModel(), entityDB.get().getModel() );
		assertNotNull( entityDB.get().getItem() );
		assertEquals( mock.getItem(), entityDB.get().getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID do relacionamento quando procurado retornar relacionamento de produto e modelo" )
	void givenExistedItemModelId_whenFind_ThenReturnItemModel() throws Exception {
		ItemModel mock = new ItemModel(1, 2000, 2005, itemMock1, modelMock1);

		ItemModel entity = ItemModel.builder()
								.id( mock.getId() )
								.build();

		List<ItemModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ItemModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getItem() );
		assertEquals( mock.getItem(), entityDB.getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ano de inicio quando procurado retornar relacionamento de produto e modelo" )
	void givenModelYearStart_whenFind_ThenReturnItemModel() throws Exception {
		ItemModel mock = new ItemModel(1, 2000, 2005, itemMock1, modelMock1);

		ItemModel entity = ItemModel.builder()
								.modelYearStart( mock.getModelYearStart() )
								.build();

		List<ItemModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ItemModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getItem() );
		assertEquals( mock.getItem(), entityDB.getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ano de fim quando procurado retornar relacionamento de produto e modelo" )
	void givenModelYearEmd_whenFind_ThenReturnItemModel() throws Exception {
		ItemModel mock = new ItemModel(1, 2000, 2005, itemMock1, modelMock1);

		ItemModel entity = ItemModel.builder()
								.modelYearEnd( mock.getModelYearEnd() )
								.build();

		List<ItemModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ItemModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getItem() );
		assertEquals( mock.getItem(), entityDB.getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o modelo quando procurado retornar relacionamento de produto e modelo" )
	void givenModel_whenFind_ThenReturnItemModel() throws Exception {
		ItemModel mock = new ItemModel(1, 2000, 2005, itemMock1, modelMock1);

		ItemModel entity = ItemModel.builder()
								.model( mock.getModel() )
								.build();

		List<ItemModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ItemModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getItem() );
		assertEquals( mock.getItem(), entityDB.getItem() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o produto quando procurado retornar relacionamento de produto e modelo" )
	void givenProduct_whenFind_ThenReturnItemModel() throws Exception {
		ItemModel mock = new ItemModel(1, 2000, 2005, itemMock1, modelMock1);

		ItemModel entity = ItemModel.builder()
								.item( mock.getItem() )
								.build();

		List<ItemModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ItemModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getItem() );
		assertEquals( mock.getItem(), entityDB.getItem() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ItemModelDAOTest#validateDuplicated")
	@DisplayName( "findDuplicated - Dado uma combinação duplicada deve retornar uma lista" )
	void whenValidate_Duplicate( ItemModel entity, boolean hasDuplicated ) throws Exception {
		List<ItemModel> listDB = dao.findDuplicated(entity);
		
		assertNotNull( listDB );
		assertEquals( listDB.isEmpty(), !hasDuplicated );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ItemModelDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( ItemModel entity ) throws Exception {
		dao.update( entity );
		
		Optional<ItemModel> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getModelYearStart(), entityDB.get().getModelYearStart());
		assertEquals( entity.getModelYearEnd(), entityDB.get().getModelYearEnd());
		assertNotNull( entityDB.get().getModel() );
		assertEquals( entity.getModel(), entityDB.get().getModel() );
		assertNotNull( entityDB.get().getItem() );
		assertEquals( entity.getItem(), entityDB.get().getItem() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedItemModel_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<ItemModel> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	// FIXME Implementar o hasPriceListRelationship
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new ItemModel(null, 2000, 2005, itemMock1, modelMock1) ),
    		Arguments.of( new ItemModel(null, 2010, 2015, itemMock2, modelMock2) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new ItemModel(1, 2000, 2015, itemMock2, modelMock2) ),
    		Arguments.of( new ItemModel(2, 2010, 2015, itemMock1, modelMock1) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> validateDuplicated() {
	    return Stream.of(
    		Arguments.of( new ItemModel(null, 1999, 2001, itemMock1, modelMock1), true ),
    		Arguments.of( new ItemModel(null, 2001, 2006, itemMock1, modelMock1), true ),
    		Arguments.of( new ItemModel(null, 1999, 2006, itemMock1, modelMock1), true ),
    		Arguments.of( new ItemModel(null, 2001, 2004, itemMock1, modelMock1), true ),
    		Arguments.of( new ItemModel(null, 1998, 1999, itemMock1, modelMock1), false ),
    		Arguments.of( new ItemModel(null, 2006, 2010, itemMock1, modelMock1), false )
	    );
	}
	
}