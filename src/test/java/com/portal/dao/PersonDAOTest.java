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

import com.portal.dao.impl.PersonDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.PersonClassification;
import com.portal.model.Person;
import com.portal.model.UserModel;
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
class PersonDAOTest {

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
	private PersonDAO dao;
	
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

		for( PersonClassification classifiers : PersonClassification.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PersonDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Person entity ) throws Exception {
		Optional<Person> entityId = dao.save( entity );
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "listAll - Quando listar todos os pessoas" )
	void whenListAll_ReturnListPerson() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "per_id");
		List<Person> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar o pessoa" )
	void givenExistedPerson_whenGetById_ThenReturnPerson() throws Exception {

		Person entity = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );

		Optional<Person> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um ID existente retornar o pessoa" )
	void givenExistedPersonId_whenFind_ThenReturnPerson() throws Exception {

		Person mock = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
		
		Person entity = Person.builder()
								.id( 1 )
								.build();

		List<Person> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock.getId(), entityDB.get(0).getId() );
		assertEquals( mock.getName(), entityDB.get(0).getName() );
		assertEquals( mock.getJobTitle(), entityDB.get(0).getJobTitle() );
		assertEquals( mock.getCpf(), entityDB.get(0).getCpf() );
		assertEquals( mock.getCnpj(), entityDB.get(0).getCnpj() );
		assertEquals( mock.getRg(), entityDB.get(0).getRg() );
		assertEquals( mock.getRne(), entityDB.get(0).getRne() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um nome existente retornar o pessoa" )
	void givenExistedPersonName_whenFind_ThenReturnPerson() throws Exception {
		
		Person mock = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
		
		Person entity = Person.builder()
								.name( "Person 1" )
								.build();

		List<Person> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock.getId(), entityDB.get(0).getId() );
		assertEquals( mock.getName(), entityDB.get(0).getName() );
		assertEquals( mock.getJobTitle(), entityDB.get(0).getJobTitle() );
		assertEquals( mock.getCpf(), entityDB.get(0).getCpf() );
		assertEquals( mock.getCnpj(), entityDB.get(0).getCnpj() );
		assertEquals( mock.getRg(), entityDB.get(0).getRg() );
		assertEquals( mock.getRne(), entityDB.get(0).getRne() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um job title retornar o pessoa" )
	void givenExistedPersonJobTitle_whenFind_ThenReturnPerson() throws Exception {
		
		Person mock = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
		
		Person entity = Person.builder()
								.jobTitle( "Job 1" )
								.build();

		List<Person> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock.getId(), entityDB.get(0).getId() );
		assertEquals( mock.getName(), entityDB.get(0).getName() );
		assertEquals( mock.getJobTitle(), entityDB.get(0).getJobTitle() );
		assertEquals( mock.getCpf(), entityDB.get(0).getCpf() );
		assertEquals( mock.getCnpj(), entityDB.get(0).getCnpj() );
		assertEquals( mock.getRg(), entityDB.get(0).getRg() );
		assertEquals( mock.getRne(), entityDB.get(0).getRne() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um CPF retornar o pessoa" )
	void givenExistedPersonCPF_whenFind_ThenReturnPerson() throws Exception {
		
		Person mock = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
		
		Person entity = Person.builder()
								.cpf( "00000000001" )
								.build();

		List<Person> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock.getId(), entityDB.get(0).getId() );
		assertEquals( mock.getName(), entityDB.get(0).getName() );
		assertEquals( mock.getJobTitle(), entityDB.get(0).getJobTitle() );
		assertEquals( mock.getCpf(), entityDB.get(0).getCpf() );
		assertEquals( mock.getCnpj(), entityDB.get(0).getCnpj() );
		assertEquals( mock.getRg(), entityDB.get(0).getRg() );
		assertEquals( mock.getRne(), entityDB.get(0).getRne() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um CNPJ retornar o pessoa" )
	void givenExistedPersonCNPJ_whenFind_ThenReturnPerson() throws Exception {
		
		Person mock = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
		
		Person entity = Person.builder()
								.cnpj( "00000000000001" )
								.build();

		List<Person> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock.getId(), entityDB.get(0).getId() );
		assertEquals( mock.getName(), entityDB.get(0).getName() );
		assertEquals( mock.getJobTitle(), entityDB.get(0).getJobTitle() );
		assertEquals( mock.getCpf(), entityDB.get(0).getCpf() );
		assertEquals( mock.getCnpj(), entityDB.get(0).getCnpj() );
		assertEquals( mock.getRg(), entityDB.get(0).getRg() );
		assertEquals( mock.getRne(), entityDB.get(0).getRne() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um RNE retornar o pessoa" )
	void givenExistedPersonRNE_whenFind_ThenReturnPerson() throws Exception {
		
		Person mock = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
		
		Person entity = Person.builder()
								.rne( "00000000001" )
								.build();

		List<Person> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock.getId(), entityDB.get(0).getId() );
		assertEquals( mock.getName(), entityDB.get(0).getName() );
		assertEquals( mock.getJobTitle(), entityDB.get(0).getJobTitle() );
		assertEquals( mock.getCpf(), entityDB.get(0).getCpf() );
		assertEquals( mock.getCnpj(), entityDB.get(0).getCnpj() );
		assertEquals( mock.getRg(), entityDB.get(0).getRg() );
		assertEquals( mock.getRne(), entityDB.get(0).getRne() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "find - Dado um RG retornar o pessoa" )
	void givenExistedPersonRG_whenFind_ThenReturnPerson() throws Exception {
		
		Person mock = new Person( 1, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
		
		Person entity = Person.builder()
								.rg( "00000000000000000001" )
								.build();

		List<Person> entityDB = dao.find( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 1, entityDB.size() );
		assertEquals( mock.getId(), entityDB.get(0).getId() );
		assertEquals( mock.getName(), entityDB.get(0).getName() );
		assertEquals( mock.getJobTitle(), entityDB.get(0).getJobTitle() );
		assertEquals( mock.getCpf(), entityDB.get(0).getCpf() );
		assertEquals( mock.getCnpj(), entityDB.get(0).getCnpj() );
		assertEquals( mock.getRg(), entityDB.get(0).getRg() );
		assertEquals( mock.getRne(), entityDB.get(0).getRne() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um NOME retornar o pessoa" )
	void givenExistedPersonbName_whenSearch_ThenReturnPerson() throws Exception {
		Person entity = Person.builder()
								.name( "Person" )
								.build();

		List<Person> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 6, entityDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um JOB TITLE retornar o pessoa" )
	void givenExistedPersonbJobTitle_whenSearch_ThenReturnPerson() throws Exception {
		Person entity = Person.builder()
								.jobTitle( "Job" )
								.build();

		List<Person> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 5, entityDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um CPF retornar o pessoa" )
	void givenExistedPersonbCPF_whenSearch_ThenReturnPerson() throws Exception {
		Person entity = Person.builder()
								.cpf( "0000000000" )
								.build();

		List<Person> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 4, entityDB.size() );
	}

	@Test
	@Order(3)
	@DisplayName( "search - Dado um CNPJ retornar o pessoa" )
	void givenExistedPersonbCNPJ_whenSearch_ThenReturnPerson() throws Exception {
		Person entity = Person.builder()
								.cnpj( "0000000000000" )
								.build();

		List<Person> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 3, entityDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um RNE retornar o pessoa" )
	void givenExistedPersonbRNE_whenSearch_ThenReturnPerson() throws Exception {
		Person entity = Person.builder()
								.rne( "0000000000" )
								.build();

		List<Person> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
	}
	
	@Test
	@Order(3)
	@DisplayName( "search - Dado um RG retornar o pessoa" )
	void givenExistedPersonbRG_whenSearch_ThenReturnPerson() throws Exception {
		Person entity = Person.builder()
								.rne( "0000000000" )
								.build();

		List<Person> entityDB = dao.search( entity, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( 2, entityDB.size() );
	}
	
	@Order(4)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PersonDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( Person entity ) throws Exception {
		dao.update( entity );
		
		Optional<Person> entityDB = dao.getById( entity.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( entity, entityDB.get() );
		assertEquals( entity.getId(), entityDB.get().getId() );
		assertEquals( entity.getName(), entityDB.get().getName() );
		assertEquals( entity.getJobTitle(), entityDB.get().getJobTitle() );
		assertEquals( entity.getCpf(), entityDB.get().getCpf() );
		assertEquals( entity.getCnpj(), entityDB.get().getCnpj() );
		assertEquals( entity.getRg(), entityDB.get().getRg() );
		assertEquals( entity.getRne(), entityDB.get().getRne() );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasPartnerRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasPartnerRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasPartnerRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasPartnerPersonRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasPartnerPersonRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasPartnerPersonRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasProposalRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasProposalRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasProposalRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasProposalDetailRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasProposalDetailRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasProposalDetailRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasCommissionRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasCommissionRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasCommissionRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasLeadRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasLeadRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasLeadRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasHoldingRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasHoldingRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasHoldingRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(5)
	@DisplayName( "hasUserRelationship - Quando não existe relacionamento retorna false" )
	void givenPerson_whenCheckHasUserRelationship_thenReturnFalse() throws Exception {
		boolean db = dao.hasUserRelationship(1);
		assertFalse( db );
	}
	
	@Test
	@Order(6)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedPerson_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Person> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Person( null, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( null, "Person 2", "Job 2", "00000000002", "00000000000002", "00000000002", null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( null, "Person 3", "Job 3", "00000000003", "00000000000003", null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( null, "Person 4", "Job 4", "00000000004", null, null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( null, "Person 5", "Job 5", null, null, null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( null, "Person 6",  null, null, null, null, null, null, PersonClassification.PF.getType() ) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Person( 1, "Person 1.1", "Job 1.1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 2, "Person 2.1", "Job 2.1", "00000000002", "00000000000002", "00000000002", null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 3, "Person 3.1", "Job 3.1", "00000000003", "00000000000003", null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 4, "Person 4.1", "Job 4.1", "00000000004", null, null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 5, "Person 5.1", "Job 5.1", null, null, null, null, null, PersonClassification.PF.getType() ) ),
    		Arguments.of( new Person( 6, "Person 6.1",  null, null, null, null, null, null, PersonClassification.PF.getType() ) )
	    );
	}
	
}
