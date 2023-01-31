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

import com.portal.dao.impl.ModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;

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
class ModelDAOTest {

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
	private ModelDAO dao;
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private ClassifierService classifierService;
	
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
		
		brandService.save( new Brand( null, "BRAND 1", true ) , profile);
		brandService.save( new Brand( null, "BRAND 2", false ) , profile);
		brandService.save( new Brand( null, "BRAND 3", true ) , profile);
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ModelDAOTest#listModelToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Model model ) throws Exception {
		Optional<Model> modelId = dao.save( model );
		assertNotNull( modelId );
		assertTrue( modelId.isPresent() );
		assertEquals( model.getId(), modelId.get().getId() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "listAll - Quando listar todos os modelos" )
	void whenListAll_ReturnListModel() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "mdl_id");
		List<Model> list = dao.listAll( pageReq  );

		assertEquals(listModelToSave().count(), list.size());
	}
	
	@Test
	@Order(4)
	@DisplayName( "getById - Dado um ID existente retornar o modelo" )
	void givenExistedModel_whenGetById_ThenReturnModel() throws Exception {
		
		Model model = Model.builder()
								.id( 1 )
								.name( "Model 1" )
								.active( true )
								.brand( Brand.builder().id(1).build() )
								.build();

		Optional<Model> modelDB = dao.getById( model.getId() );
		
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( model, modelDB.get() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um ID existente retornar o modelo" )
	void givenExistedModelId_whenFind_ThenReturnModel() throws Exception {
		
		Model model = Model.builder()
								.id( 1 )
								.build();

		List<Model> modelDB = dao.find( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( modelDB.get(0).getId(), model.getId() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um nome existente retornar o modelo" )
	void givenExistedModelName_whenFind_ThenReturnModel() throws Exception {
		
		Model model = Model.builder()
								.name( "Model 1" )
								.build();

		List<Model> modelDB = dao.find( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( 1, modelDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado uma flag retornar o modelo" )
	void givenExistedModelActive_whenFind_ThenReturnModel() throws Exception {
		
		Model model = Model.builder()
								.active( true )
								.build();

		List<Model> modelDB = dao.find( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( 2, modelDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado uma marca retorna a lista de modelos" )
	void givenBrandId_whenFind_ThenReturnModels() throws Exception {
		
		Model model = Model.builder()
								.brand( Brand.builder().id(1).build() )
								.build();

		List<Model> modelDB = dao.find( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( 1, modelDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um ID existente retornar o modelo" )
	void givenExistedModelId_whenSearch_ThenReturnModel() throws Exception {
		
		Model model = Model.builder()
								.id( 3 )
								.build();

		List<Model> modelDB = dao.search( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( modelDB.get(0).getId(), model.getId() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um nome existente retornar o modelo" )
	void givenExistedModelName_whenSearch_ThenReturnModel() throws Exception {
		
		Model model = Model.builder()
								.name( "Model" )
								.build();

		List<Model> modelDB = dao.search( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( 3, modelDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado uma flag retornar o modelo" )
	void givenExistedModelActive_whenSearch_ThenReturnModel() throws Exception {
		Model model = Model.builder()
								.active( false )
								.build();

		List<Model> modelDB = dao.search( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( 1, modelDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado uma marca retorna a lista de modelos" )
	void givenBrandId_whenSearch_ThenReturnModels() throws Exception {
		
		Model model = Model.builder()
								.brand( Brand.builder().id(1).build() )
								.build();

		List<Model> modelDB = dao.search( model, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( 1, modelDB.size() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.ModelDAOTest#listModelToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( Model model ) throws Exception {
		dao.update( model );
		
		Optional<Model> modelDB = dao.getById( model.getId() );
		
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( model, modelDB.get() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasLeadRelationship - Quando não existe relacionamento com lead retorna false" )
	void givenModel_whenCheckHasLeadRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasLeadRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasItemRelationship - Quando não existe relacionamento com item retorna false" )
	void givenModel_whenCheckHasItemRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasItemRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasProductRelationship - Quando não existe relacionamento com produto retorna false" )
	void givenModel_whenCheckHasProductRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasProductRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasVehicleRelationship - Quando não existe relacionamento com veículo retorna false" )
	void givenModel_whenCheckHasVehicleRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasVehicleRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(6)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedModel_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Model> modelDB = dao.getById( id );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listModelToSave() {
	    return Stream.of(
    		Arguments.of( new Model( null, "Model 1", true, Brand.builder().id(1).build(), "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM ) ),
    		Arguments.of( new Model( null, "Model 2", true, Brand.builder().id(2).build(), "038002-5", ModelBodyType.SEDAN, ModelSize.MEDIUM, ModelCategory.STANDARD ) ),
    		Arguments.of( new Model( null, "Model 3", false, Brand.builder().id(3).build(), "038002-6", ModelBodyType.SUV, ModelSize.LARGE, ModelCategory.PREMIUM ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listModelToUpdate() {
	    return Stream.of(
    		Arguments.of( new Model( 1, "Model 1.1", true, Brand.builder().id(1).build(), "038002-4", ModelBodyType.SEDAN, ModelSize.SMALL, ModelCategory.STANDARD ) ),
    		Arguments.of( new Model( 2, "Model 2", true, Brand.builder().id(2).build(), "038002-5", ModelBodyType.SEDAN, ModelSize.SMALL, ModelCategory.PREMIUM ) ),
    		Arguments.of( new Model( 3, "Model 3.3", false, Brand.builder().id(2).build(), "038002-6", ModelBodyType.HATCH, ModelSize.MEDIUM, ModelCategory.PREMIUM ) )
	    );
	}
	
}
