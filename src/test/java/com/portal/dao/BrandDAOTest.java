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

import com.portal.dao.impl.BrandDAO;
import com.portal.model.Brand;

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
class BrandDAOTest {

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
	private BrandDAO dao;
	
	@Order(1)
	@ParameterizedTest
	@MethodSource("com.portal.dao.BrandDAOTest#listBrandToSave")
	void whenSave_ReturnSavedId( Brand brand ) throws Exception {
		
		Optional<Brand> brandId = dao.save( brand );
		
		assertNotNull( brandId );
		assertTrue( brandId.isPresent() );
		assertEquals( brand.getId(), brandId.get().getId() );
	}
	
	@Test
	@Order(2)
	void whenListAll_ReturnListBrand() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "brd_id");
		List<Brand> list = dao.listAll( pageReq  );

		assertEquals(listBrandToSave().count(), list.size());
	}
	
	@Test
	@Order(2)
	void givenExistedBrand_whenGetById_ThenReturnBrand() throws Exception {
		
		Brand brand = Brand.builder()
								.id( 1 )
								.name( "BRAND 1" )
								.active( true )
								.build();

		Optional<Brand> brandDB = dao.getById( brand.getId() );
		
		assertNotNull( brandDB );
		assertTrue( brandDB.isPresent() );
		assertEquals( brand, brandDB.get() );
	}
	
	@Test
	@Order(2)
	void givenExitedBrandId_whenFind_ThenReturnBrand() throws Exception {
		
		Brand brand = Brand.builder()
								.id( 1 )
								.build();

		List<Brand> brandDB = dao.find( brand, null );
		
		assertNotNull( brandDB );
		assertFalse( brandDB.isEmpty() );
		assertEquals( brandDB.get(0).getId(), brand.getId() );
	}
	
	@Test
	@Order(2)
	void givenExitedBrandName_whenFind_ThenReturnBrand() throws Exception {
		
		Brand brand = Brand.builder()
								.name( "BRAND 1" )
								.build();

		List<Brand> brandDB = dao.find( brand, null );
		
		assertNotNull( brandDB );
		assertFalse( brandDB.isEmpty() );
		assertEquals( 1, brandDB.size() );
	}
	
	@Test
	@Order(2)
	void givenExitedBrandActive_whenFind_ThenReturnBrand() throws Exception {
		
		Brand brand = Brand.builder()
								.active( true )
								.build();

		List<Brand> brandDB = dao.find( brand, null );
		
		assertNotNull( brandDB );
		assertFalse( brandDB.isEmpty() );
		assertEquals( 2, brandDB.size() );
	}
	
	@Test
	@Order(2)
	void givenExitedBrandId_whenSearch_ThenReturnBrand() throws Exception {
		
		Brand brand = Brand.builder()
								.id( 3 )
								.build();

		List<Brand> brandDB = dao.search( brand, null );
		
		assertNotNull( brandDB );
		assertFalse( brandDB.isEmpty() );
		assertEquals( brandDB.get(0).getId(), brand.getId() );
	}
	
	@Test
	@Order(2)
	void givenExitedBrandName_whenSearch_ThenReturnBrand() throws Exception {
		
		Brand brand = Brand.builder()
								.name( "BRAND" )
								.build();

		List<Brand> brandDB = dao.search( brand, null );
		
		assertNotNull( brandDB );
		assertFalse( brandDB.isEmpty() );
		assertEquals( 3, brandDB.size() );
	}
	
	
	@Test
	@Order(2)
	void givenExitedBrandActive_whenSearch_ThenReturnBrand() throws Exception {
		Brand brand = Brand.builder()
								.active( false )
								.build();

		List<Brand> brandDB = dao.search( brand, null );
		
		assertNotNull( brandDB );
		assertFalse( brandDB.isEmpty() );
		assertEquals( 1, brandDB.size() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.BrandDAOTest#listBrandToUpdate")
	void whenUpdate_CheckNewValues( Brand brand ) throws Exception {
		dao.update( brand );
		
		Optional<Brand> brandDB = dao.getById( brand.getId() );
		
		assertNotNull( brandDB );
		assertTrue( brandDB.isPresent() );
		assertEquals( brand, brandDB.get() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasModelRelationship - Quando não existe relacionamento retorna false" )
	void givenBrand_whenCheckHasModelRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasModelRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasPartnerRelationship - Quando não existe relacionamento retorna false" )
	void givenBrand_whenCheckHasPartnerRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasPartnerRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasLeadRelationship - Quando não existe relacionamento retorna false" )
	void givenBrand_whenCheckHasLeadRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasLeadRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(6)
	void givenExitedBrand_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Brand> brandDB = dao.getById( id );
		
		assertNotNull( brandDB );
		assertFalse( brandDB.isPresent() );
	}
	
	private static Stream<Arguments> listBrandToSave() {
	    return Stream.of(
    		Arguments.of( new Brand( null, "BRAND 1", true) ),
    		Arguments.of( new Brand( null, "BRAND 2", false ) ),
    		Arguments.of( new Brand( null, "BRAND 3", true ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listBrandToUpdate() {
	    return Stream.of(
    		Arguments.of( new Brand( 1, "BRAND 1.1", true ) ),
    		Arguments.of( new Brand( 2, "BRAND 2", true ) ),
    		Arguments.of( new Brand( 3, "BRAND 3.2", false ) )
	    );
	}
	
}
