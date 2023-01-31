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

import com.portal.dao.impl.LeadDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.LeadState;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.enums.PersonClassification;
import com.portal.enums.SaleProbabilty;
import com.portal.model.Brand;
import com.portal.model.Lead;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.model.Seller;
import com.portal.model.Source;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.ModelService;
import com.portal.service.imp.PersonService;
import com.portal.service.imp.SellerService;
import com.portal.service.imp.SourceService;

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
class LeadDAOTest {

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
	private LeadDAO dao;
	
	@Autowired
	private ClassifierService classifierService;
	
	@Autowired
	private SourceService sourceService;
	
	@Autowired
	private PersonService personService;

	@Autowired
	private SellerService sellerService;
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private ModelService modelService;
	 
	private static final Person clientMock = new Person(null, "Person Client", "Client", "00000000001", null, null, null, null, PersonClassification.PF.getType());
	private static final Seller sellerMock = Seller.builder().person( new Person( null, "Person Seller", "Seller", "00000000002", null, null, null, null, PersonClassification.PF.getType() )).build();
	private static final Source sourceMock = new Source( null, "Source 1", true );
	private static final Brand brandMock = new Brand( null, "Brand 1", true );
	private static final Model modelMock = new Model( null, "Model 1", true, brandMock, "038002-4", ModelBodyType.HATCH, ModelSize.SMALL, ModelCategory.PREMIUM );
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
		
		for( PersonClassification classifiers : PersonClassification.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}
		
		for( LeadState classifiers : LeadState.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}

		Optional<Person> dbPerson = this.personService.save(clientMock, profile);
		clientMock.setId( dbPerson.get().getId() );
		
		Optional<Seller> dbSeller = this.sellerService.save(sellerMock, profile);
		sellerMock.setId( dbSeller.get().getId() );
		
		Optional<Source> dbSource = this.sourceService.save(sourceMock, profile);
		sourceMock.setId( dbSource.get().getId() );
		
		Optional<Brand> dbBrand = this.brandService.save(brandMock, profile);
		brandMock.setId( dbBrand.get().getId() );
		
		Optional<Model> dbModel = this.modelService.save(modelMock, profile);
		modelMock.setId( dbModel.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.LeadDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Lead entity ) throws Exception {
		Optional<Lead> entityId = dao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "listAll - Quando listar todos as leads" )
	void whenListAll_ReturnListLead() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "led_id");
		List<Lead> list = dao.listAll( pageReq  );

		assertEquals( listEntityToSave().count(), list.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar a lead" )
	void givenExistedLead_whenGetById_ThenReturnLead() throws Exception {
		Lead mock = Lead.builder().id(1).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
				.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType())
				.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 1").subject("").build();

		Optional<Lead> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getCreateDate(), entityDB.get().getCreateDate() );
		assertEquals( mock.getDescription(), entityDB.get().getDescription() );
		assertNotNull( entityDB.get().getClient() );
		assertEquals( mock.getClient(), entityDB.get().getClient() );
		assertNotNull( entityDB.get().getSeller() );
		assertEquals( mock.getSeller(), entityDB.get().getSeller() );
		assertNotNull( entityDB.get().getSource() );
		assertEquals( mock.getSource(), entityDB.get().getSource() );
		assertNotNull( entityDB.get().getStatus() );
		assertEquals( mock.getStatus(), entityDB.get().getStatus() );
		assertNotNull( entityDB.get().getModel() );
		assertEquals( mock.getModel(), entityDB.get().getModel() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.LeadDAOTest#listEntityToFind")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenFind_ReturnLead( Lead mock, Lead filter, int expectedSize ) throws Exception {
		
		List<Lead> entityDB = dao.find( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<Lead> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertNotNull( entity );
		assertTrue( entity.isPresent() );
		assertEquals( mock, entity.get() );
		assertEquals( mock.getCreateDate(), entity.get().getCreateDate() );
		assertEquals( mock.getDescription(), entity.get().getDescription() );
		assertNotNull( entity.get().getClient() );
		assertEquals( mock.getClient(), entity.get().getClient() );
		assertNotNull( entity.get().getSeller() );
		assertEquals( mock.getSeller(), entity.get().getSeller() );
		assertNotNull( entity.get().getSource() );
		assertEquals( mock.getSource(), entity.get().getSource() );
		assertNotNull( entity.get().getStatus() );
		assertEquals( mock.getStatus(), entity.get().getStatus() );
		assertNotNull( entity.get().getModel() );
		assertEquals( mock.getModel(), entity.get().getModel() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.LeadDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( Lead mock ) throws Exception {
		dao.update( mock );
		
		Optional<Lead> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getCreateDate(), entityDB.get().getCreateDate() );
		assertEquals( mock.getDescription(), entityDB.get().getDescription() );
		assertNotNull( entityDB.get().getClient() );
		assertEquals( mock.getClient(), entityDB.get().getClient() );
		assertNotNull( entityDB.get().getSeller() );
		assertEquals( mock.getSeller(), entityDB.get().getSeller() );
		assertNotNull( entityDB.get().getSource() );
		assertEquals( mock.getSource(), entityDB.get().getSource() );
		assertNotNull( entityDB.get().getStatus() );
		assertEquals( mock.getStatus(), entityDB.get().getStatus() );
		assertNotNull( entityDB.get().getModel() );
		assertEquals( mock.getModel(), entityDB.get().getModel() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedLead_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Lead> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of(Lead.builder().id(null).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
				.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.OPENED.getType())
				.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 1").subject("").build()),
	    		
    		Arguments.of(Lead.builder().id(null).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
    				.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.UNCONVERTED.getType())
    				.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 2").subject("").build()),
	    		
	    		
    		Arguments.of(Lead.builder().id(null).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
    				.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.UNCONVERTED.getType())
    				.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 3").subject("").build()),
    		
    		Arguments.of(Lead.builder().id(null).createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00))
    				.client(clientMock).seller(sellerMock).source(sourceMock).status(LeadState.UNCONVERTED.getType())
    				.model(modelMock).saleProbabilty(SaleProbabilty.HIGH.getType()).description("NOTES 4").subject("").build())    		
	    );
	}
	/*
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), null, "NOTES 1.1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, "") ),
    		Arguments.of( new Lead(2, LocalDateTime.of(2020, 11, 15, 00, 00, 00, 00), LocalDateTime.of(2020, 12, 15, 00, 00, 00, 00), "NOTES 2.1", clientMock, sellerMock, sourceMock, LeadState.CONVERTED, modelMock, brandMock, SaleProbabilty.HIGH, "") ),
    		Arguments.of( new Lead(3, LocalDateTime.of(2021, 12, 15, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 20, 00, 00, 00, 00), "NOTES 3.1", clientMock, sellerMock, sourceMock, LeadState.UNCONVERTED, modelMock, brandMock, SaleProbabilty.HIGH, "") )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
	    return Stream.of(
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().id( 1 ).build(), 1 ),
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().createDate(LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00)).build(), 4 ),
    		Arguments.of( new Lead(2, LocalDateTime.of(2020, 11, 10, 00, 00, 00, 00), LocalDateTime.of(2020, 12, 10, 00, 00, 00, 00), "NOTES 2", clientMock, sellerMock, sourceMock, LeadState.CONVERTED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().endDate(LocalDateTime.of(2020, 12, 10, 00, 00, 00, 00)).build(), 1 ),
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().client(clientMock).build(), 5 ),
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().seller(sellerMock).build(), 5 ),
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().source(sourceMock).build(), 5 ),
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().status( LeadState.OPENED ).build(), 1 ),
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().model(modelMock).build(), 4 ),
    		Arguments.of( new Lead(1, LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), null, "NOTES 1", clientMock, sellerMock, sourceMock, LeadState.OPENED, modelMock, brandMock, SaleProbabilty.HIGH, ""), Lead.builder().brand(brandMock).build(), 3 )
	    );
	}*/
	
}
