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

import com.portal.dao.impl.PriceProductDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Model;
import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ModelService;
import com.portal.service.imp.PriceListService;
import com.portal.service.imp.ProductModelService;
import com.portal.service.imp.ProductService;

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
class PriceProductDAOTest {

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
	private PriceProductDAO dao;

	@Autowired
	private ProductModelService productModelService;
	
	@Autowired
	private ModelService modelService;
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private PriceListService priceListService;
	
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private ClassifierService classifierService;
	
	private static Channel channelMock = new Channel( null, "Channel 1", true, true, true);
	private static Brand brandMock = new Brand( null, "BRAND 1", true );
	private static Model modelMock = new Model( null, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static Product productMock = new Product( null, "PRODUCT 1", true, 10, null);
	private static PriceList priceListMock1 = new PriceList( null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	private static PriceList priceListMock2 = new PriceList( null, "PriceList 2", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channelMock, false);
	private static ProductModel productModelMock1 = new ProductModel( null, false, 2000, 2015, 10, productMock, modelMock);
	private static ProductModel productModelMock2 = new ProductModel( null, false, 2016, 2020, 30, productMock, modelMock);
	
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
		
		Optional<Channel> dbChannel = channelService.save( channelMock, profile);
		channelMock.setId( dbChannel.get().getId() );
		Optional<Brand> dbBrand = brandService.save( brandMock, profile);
		brandMock.setId( dbBrand.get().getId() );
		Optional<Model> dbModel = modelService.save( modelMock, profile);
		modelMock.setId( dbModel.get().getId() );
		Optional<Product> dbProduct = productService.save( productMock, profile);
		productMock.setId( dbProduct.get().getId() );
		Optional<PriceList> dbPricelist = priceListService.save( priceListMock1, profile);
		priceListMock1.setId( dbPricelist.get().getId() );
		Optional<PriceList> dbPricelist2 = priceListService.save( priceListMock2, profile);
		priceListMock2.setId( dbPricelist2.get().getId() );
		Optional<ProductModel> dbProductModel = productModelService.save( productModelMock1, profile);
		productModelMock1.setId( dbProductModel.get().getId() );
		Optional<ProductModel> dbProductModel2 = productModelService.save( productModelMock2, profile);
		productModelMock2.setId( dbProductModel2.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceProductDAOTest#listEntityToSave")
	@DisplayName( "save - Quando salva o relacionamento então não retorna erro" )
	void whenSave_ThenNoError( PriceProduct model ) throws Exception {
		assertDoesNotThrow(()->dao.save( model ));
	}
	
	@Test
	@Order(3)
	void givenExistedProductPrice_whenGetById_ThenReturn() throws Exception {
		PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);

		Optional<PriceProduct> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getProductModel() );
		assertEquals( mock.getProductModel(), entityDB.get().getProductModel() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID do relacionamento quando procurado retornar a entidade" )
	void givenExistedProductPriceId_whenFind_ThenReturnEntity() throws Exception {
		PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);

		PriceProduct findEntity = PriceProduct.builder()
											.id( mock.getId() )
											.build();

		List<PriceProduct> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		PriceProduct entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getPrice(), entityDB.getPrice() );
		assertNotNull( entityDB.getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.getPriceList() );
		assertNotNull( entityDB.getProductModel() );
		assertEquals( mock.getProductModel(), entityDB.getProductModel() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID da lista de preço quando procurado retornar a entidade" )
	void givenExistedPriceList_whenFind_ThenReturnEntity() throws Exception {
		PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);

		PriceProduct findEntity = PriceProduct.builder()
												.priceList( priceListMock1 )
												.build();

		List<PriceProduct> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 2, listDB.size() );

		Optional<PriceProduct> entityDB = listDB.stream().filter( item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getProductModel() );
		assertEquals( mock.getProductModel(), entityDB.get().getProductModel() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID do modelo de produto quando procurado retornar a entidade" )
	void givenExistedProductModel_whenFind_ThenReturnEntity() throws Exception {
		PriceProduct mock = new PriceProduct(1, 100d, priceListMock1, productModelMock1);

		PriceProduct findEntity = PriceProduct.builder()
												.productModel(productModelMock1)
												.build();

		List<PriceProduct> listDB = dao.find( findEntity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		Optional<PriceProduct> entityDB = listDB.stream().filter( item->item.getId().equals(1) ).findFirst();
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( mock.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getProductModel() );
		assertEquals( mock.getProductModel(), entityDB.get().getProductModel() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceProductDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( PriceProduct entity ) throws Exception {
		dao.update( entity );
		
		Optional<PriceProduct> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getPrice(), entityDB.get().getPrice() );
		assertNotNull( entityDB.get().getPriceList() );
		assertEquals( entity.getPriceList(), entityDB.get().getPriceList() );
		assertNotNull( entityDB.get().getProductModel() );
		assertEquals( entity.getProductModel(), entityDB.get().getProductModel() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedProductPrice_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		dao.delete( id );
		Optional<PriceProduct> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new PriceProduct(null, 100d, priceListMock1, productModelMock1) ),
			Arguments.of( new PriceProduct(null, 200d, priceListMock1, productModelMock2 ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new PriceProduct(1, 150d, priceListMock2, productModelMock2) ),
			Arguments.of( new PriceProduct(2, 250d, priceListMock2, productModelMock1 ) )
	    );
	}
}
