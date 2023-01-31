package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dao.impl.VehicleDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.UserModel;
import com.portal.model.VehicleModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;
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
class VehicleDAOTest {

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
	private VehicleDAO dao;

	@Autowired
	private BrandService brandService;

	@Autowired
	private ModelService modelService;

	@Autowired
	private ClassifierService classifierService;

	private static Brand brandMock = new Brand( null, "BRAND 1", true );
	private static Model modelMock1 = new Model( null, "MODEL 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
	private static Model modelMock2 = new Model( null, "MODEL 2", true, brandMock, "038002-5", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );

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
	}

	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.VehicleDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( VehicleModel entity ) throws Exception {
		Optional<VehicleModel> entityId = dao.save( entity );

		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}

	@Order(3)
	@DisplayName( "listAll - Quando listar todos as fontes" )
	@ParameterizedTest
	@MethodSource("com.portal.dao.VehicleDAOTest#whenListAllthenReturnEntityList")
	void whenListAll_ReturnListVehicle( int page, int size, String sortDir, String sort, int validId ) throws Exception {

		PageRequest pageReq = PageRequest.of(page, size, Direction.fromString( sortDir ), sort);
		List<VehicleModel> list = dao.listAll( pageReq  );

		assertNotNull( list );
		assertEquals( size, list.size());

		VehicleModel dbVehicle = list.get( 0 );

		assertNotNull( dbVehicle );
		assertEquals( validId, dbVehicle.getId());
	}

	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar o veículo" )
	void givenExistedVehicle_whenGetById_ThenReturnVehicle() throws Exception {

		VehicleModel mock = new VehicleModel(1, "111111111111112", "aaa12345", modelMock1, null,2000, null, null, null, null);

		Optional<VehicleModel> entityDB = dao.getById( mock.getId() );

		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getId(), entityDB.get().getId() );
		assertEquals( mock.getChassi(), entityDB.get().getChassi() );
		assertEquals( mock.getPlate(), entityDB.get().getPlate() );
		assertNotNull( entityDB.get().getModel() );
		assertEquals( mock.getModel(), entityDB.get().getModel() );
		assertEquals( mock.getModelYear(), entityDB.get().getModelYear() );
		assertEquals( mock.getPurchaseDate(), entityDB.get().getPurchaseDate() );
		assertEquals( mock.getPurchaseValue(), entityDB.get().getPurchaseValue() );
	}

	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.VehicleDAOTest#listEntityToFind")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenFind_ReturnProposal(VehicleModel mock, VehicleModel filter, int expectedSize ) throws Exception {
		List<VehicleModel> entityDB = dao.find( filter, null );

		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );

		Optional<VehicleModel> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();

		assertEquals( mock, entity.get() );
		assertEquals( mock.getId(), entity.get().getId() );
		assertEquals( mock.getChassi(), entity.get().getChassi() );
		assertEquals( mock.getPlate(), entity.get().getPlate() );
		assertNotNull( entity.get().getModel() );
		assertEquals( mock.getModel(), entity.get().getModel() );
		assertEquals( mock.getModelYear(), entity.get().getModelYear() );
		assertEquals( mock.getPurchaseDate(), entity.get().getPurchaseDate() );
		assertEquals( mock.getPurchaseValue(), entity.get().getPurchaseValue() );
	}


	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.VehicleDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( VehicleModel mock ) throws Exception {
		dao.update( mock );

		Optional<VehicleModel> entityDB = dao.getById( mock.getId() );

		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getId(), entityDB.get().getId() );
		assertEquals( mock.getChassi(), entityDB.get().getChassi() );
		assertEquals( mock.getPlate(), entityDB.get().getPlate() );
		assertNotNull( entityDB.get().getModel() );
		assertEquals( mock.getModel(), entityDB.get().getModel() );
		assertEquals( mock.getModelYear(), entityDB.get().getModelYear() );
		assertEquals( mock.getPurchaseDate(), entityDB.get().getPurchaseDate() );
		assertEquals( mock.getPurchaseValue(), entityDB.get().getPurchaseValue() );
	}

	@Test
	@Order(4)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedVehicle_whenDelete_ThenNoFind() throws Exception {
		int id = 2;

		dao.delete( id );

		Optional<VehicleModel> entityDB = dao.getById( id );

		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
			Arguments.of(0, 3, "DESC", "vhe_id", 3),
			Arguments.of(0, 1, "DESC", "vhe_id", 3),
			Arguments.of(0, 1, "DESC", "mdl_id", 1),
			Arguments.of(0, 1, "DESC", "plate", 3),
			Arguments.of(1, 1, "DESC", "vhe_id", 2),
			Arguments.of(0, 1, "ASC", "vhe_id", 1)
	    );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new VehicleModel(null, "111111111111112", "aaa12345", modelMock1, null,2000, null, null, null, null) ),
    		Arguments.of( new VehicleModel(null, "222222222222222", "bbb12345", modelMock1, null,2020, LocalDate.of(2019, 10, 10), 10000d,null, null) ),
    		Arguments.of( new VehicleModel(null, "333333333333333", "ccc12345", modelMock1, null,2022, LocalDate.of(2019, 10, 20), 15000d, null, null) )
	    );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new VehicleModel(1, "111111111111112", "aaa123456", modelMock2, null,2001, null, null, null, null) ),
    		Arguments.of( new VehicleModel(2, "222222222222223", "bbb123456", modelMock1, null,2021,  LocalDate.of(2019, 10, 10), 10000d, null, null) ),
    		Arguments.of( new VehicleModel(3, "333333333333333", "ccc12345", modelMock1, null,2023, LocalDate.of(2020, 10, 20), 20000d, null, null) )
	    );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {

		VehicleModel mock = new VehicleModel(2, "222222222222222", "bbb12345", modelMock1, null,2020, LocalDate.of(2019, 10, 10), 10000d, null, null);

	    return Stream.of(
    		Arguments.of( mock, VehicleModel.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().chassi( mock.getChassi() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().plate( mock.getPlate() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().model( mock.getModel() ).build(), 3 ),
    		Arguments.of( mock, VehicleModel.builder().modelYear( mock.getModelYear() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().purchaseDate( mock.getPurchaseDate() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().purchaseValue( mock.getPurchaseValue() ).build(), 1 )
	    );
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSearch() {

		VehicleModel mock = new VehicleModel(1, "111111111111112", "aaa12345", modelMock1, null,2020, LocalDate.of(2019, 10, 10), 10000d, null, null);

	    return Stream.of(
    		Arguments.of( mock, VehicleModel.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().chassi( "2" ).build(), 2 ),
    		Arguments.of( mock, VehicleModel.builder().plate( "12345" ).build(), 3 ),
    		Arguments.of( mock, VehicleModel.builder().model( mock.getModel() ).build(), 3 ),
    		Arguments.of( mock, VehicleModel.builder().modelYear( mock.getModelYear() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().purchaseDate( mock.getPurchaseDate() ).build(), 1 ),
    		Arguments.of( mock, VehicleModel.builder().purchaseValue( mock.getPurchaseValue() ).build(), 1 )
	    );
	}

}
