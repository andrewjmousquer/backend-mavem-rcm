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

import com.portal.dao.impl.ProductModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ModelService;
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
class ProductModelDAOTest {

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
	private ProductModelDAO dao;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ModelService modelService;
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private ClassifierService classifierService;
	
	private static Brand brandMock = new Brand( null, "BRAND 1", true );
	private static Model modelMock1 = new Model( null, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static Model modelMock2 = new Model( null, "MODEL 2", true, brandMock, "038002-5", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static Product productMock1 = new Product( null, "PRODUCT 1", true, 10, null);
	private static Product productMock2 = new Product( null, "PRODUCT 2", true, 10, null);
	
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
		Optional<Product> dbProduct = productService.save(productMock1, profile);
		productMock1.setId( dbProduct.get().getId() );
		Optional<Product> dbProduct2 = productService.save(productMock2, profile);
		productMock2.setId( dbProduct2.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ProductModelDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( ProductModel entity ) throws Exception {
		Optional<ProductModel> entityId = dao.save( entity );
		
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
		
		Optional<ProductModel> entityDB = dao.getById( entityId.get().getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity.getHasProject(), entityDB.get().getHasProject() );
		assertEquals( entity.getModelYearStart(), entityDB.get().getModelYearStart());
		assertEquals( entity.getModelYearEnd(), entityDB.get().getModelYearEnd());
		assertEquals( entity.getManufactureDays(), entityDB.get().getManufactureDays() );
		assertNotNull( entityDB.get().getModel() );
		assertEquals( entity.getModel(), entityDB.get().getModel() );
		assertNotNull( entityDB.get().getProduct() );
		assertEquals( entity.getProduct(), entityDB.get().getProduct() );
		
	}
	
	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar o relacionamento de produto e modelo" )
	void givenExistedProductModel_whenGetById_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		Optional<ProductModel> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getHasProject(), entityDB.get().getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.get().getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.get().getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.get().getManufactureDays() );
		assertNotNull( entityDB.get().getModel() );
		assertEquals( mock.getModel(), entityDB.get().getModel() );
		assertNotNull( entityDB.get().getProduct() );
		assertEquals( mock.getProduct(), entityDB.get().getProduct() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ID do relacionamento quando procurado retornar relacionamento de produto e modelo" )
	void givenExistedProductModelId_whenFind_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		ProductModel entity = ProductModel.builder()
								.id( mock.getId() )
								.build();

		List<ProductModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ProductModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getHasProject(), entityDB.getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.getManufactureDays() );
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getProduct() );
		assertEquals( mock.getProduct(), entityDB.getProduct() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado a flag se existe projeto quando procurado retornar relacionamento de produto e modelo" )
	void givenHasProjectFlag_whenFind_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		ProductModel entity = ProductModel.builder()
								.hasProject( mock.getHasProject() )
								.build();

		List<ProductModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ProductModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getHasProject(), entityDB.getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.getManufactureDays() );
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getProduct() );
		assertEquals( mock.getProduct(), entityDB.getProduct() );
	}

	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ano de inicio quando procurado retornar relacionamento de produto e modelo" )
	void givenModelYearStart_whenFind_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		ProductModel entity = ProductModel.builder()
								.modelYearStart( mock.getModelYearStart() )
								.build();

		List<ProductModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ProductModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getHasProject(), entityDB.getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.getManufactureDays() );
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getProduct() );
		assertEquals( mock.getProduct(), entityDB.getProduct() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o ano de fim quando procurado retornar relacionamento de produto e modelo" )
	void givenModelYearEmd_whenFind_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		ProductModel entity = ProductModel.builder()
								.modelYearEnd( mock.getModelYearEnd() )
								.build();

		List<ProductModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ProductModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getHasProject(), entityDB.getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.getManufactureDays() );
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getProduct() );
		assertEquals( mock.getProduct(), entityDB.getProduct() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o número de dias de fabricação quando procurado retornar relacionamento de produto e modelo" )
	void givenManufacturerDays_whenFind_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		ProductModel entity = ProductModel.builder()
								.manufactureDays( mock.getManufactureDays() )
								.build();

		List<ProductModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ProductModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getHasProject(), entityDB.getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.getManufactureDays() );
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getProduct() );
		assertEquals( mock.getProduct(), entityDB.getProduct() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o modelo quando procurado retornar relacionamento de produto e modelo" )
	void givenModel_whenFind_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		ProductModel entity = ProductModel.builder()
								.model( mock.getModel() )
								.build();

		List<ProductModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ProductModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getHasProject(), entityDB.getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.getManufactureDays() );
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getProduct() );
		assertEquals( mock.getProduct(), entityDB.getProduct() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado o produto quando procurado retornar relacionamento de produto e modelo" )
	void givenProduct_whenFind_ThenReturnProductModel() throws Exception {
		ProductModel mock = new ProductModel(1, true, 2000, 2005, 30, productMock1, modelMock1);

		ProductModel entity = ProductModel.builder()
								.product( mock.getProduct() )
								.build();

		List<ProductModel> listDB = dao.find( entity, null );
		
		assertNotNull( listDB );
		assertEquals( 1, listDB.size() );

		ProductModel entityDB = listDB.get(0);
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB );
		assertEquals( mock.getHasProject(), entityDB.getHasProject() );
		assertEquals( mock.getModelYearStart(), entityDB.getModelYearStart());
		assertEquals( mock.getModelYearEnd(), entityDB.getModelYearEnd());
		assertEquals( mock.getManufactureDays(), entityDB.getManufactureDays() );
		assertNotNull( entityDB.getModel() );
		assertEquals( mock.getModel(), entityDB.getModel() );
		assertNotNull( entityDB.getProduct() );
		assertEquals( mock.getProduct(), entityDB.getProduct() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ProductModelDAOTest#validateDuplicated")
	@DisplayName( "findDuplicated - Dado uma combinação duplicada deve retornar uma lista" )
	void whenValidate_Duplicate( ProductModel entity, boolean hasDuplicated ) throws Exception {
		List<ProductModel> listDB = dao.findDuplicated(entity);
		
		assertNotNull( listDB );
		assertEquals( listDB.isEmpty(), !hasDuplicated );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ProductModelDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( ProductModel entity ) throws Exception {
		dao.update( entity );
		
		Optional<ProductModel> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getHasProject(), entityDB.get().getHasProject() );
		assertEquals( entity.getModelYearStart(), entityDB.get().getModelYearStart());
		assertEquals( entity.getModelYearEnd(), entityDB.get().getModelYearEnd());
		assertEquals( entity.getManufactureDays(), entityDB.get().getManufactureDays() );
		assertNotNull( entityDB.get().getModel() );
		assertEquals( entity.getModel(), entityDB.get().getModel() );
		assertNotNull( entityDB.get().getProduct() );
		assertEquals( entity.getProduct(), entityDB.get().getProduct() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedProductModel_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<ProductModel> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	// FIXME Implementar o hasPriceListRelationship
	// FIXME Implementar o findDuplicated
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new ProductModel(null, true, 2000, 2005, 30, productMock1, modelMock1) ),
    		Arguments.of( new ProductModel(null, false, 2010, 2015, 1, productMock2, modelMock2) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new ProductModel(1, false, 2000, 2015, 10, productMock2, modelMock2) ),
    		Arguments.of( new ProductModel(2, true, 2010, 2015, 15, productMock1, modelMock1) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> validateDuplicated() {
	    return Stream.of(
    		Arguments.of( new ProductModel(null, false, 1999, 2001, 1, productMock1, modelMock1), true ),
    		Arguments.of( new ProductModel(null, false, 2001, 2006, 1, productMock1, modelMock1), true ),
    		Arguments.of( new ProductModel(null, false, 1999, 2006, 1, productMock1, modelMock1), true ),
    		Arguments.of( new ProductModel(null, false, 2001, 2004, 1, productMock1, modelMock1), true ),
    		Arguments.of( new ProductModel(null, false, 1998, 1999, 1, productMock1, modelMock1), false ),
    		Arguments.of( new ProductModel(null, false, 2006, 2010, 1, productMock1, modelMock1), false )
	    );
	}
	
}
