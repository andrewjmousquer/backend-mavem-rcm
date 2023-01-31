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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dao.impl.DocumentDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.PersonClassification;
import com.portal.model.Classifier;
import com.portal.model.Document;
import com.portal.model.Person;
import com.portal.model.UserModel;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PersonService;
import com.portal.service.imp.UserService;

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
class DocumentDAOTest {

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
	private DocumentDAO dao;
	
	@Autowired
	private ClassifierService classifierService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PersonService personService;
	
	private static final Classifier userTypeMock = new Classifier(1, "ADMINISTRATOR", "USER_TYPE", "", "");
	private static final Person personMock = new Person(null, "Person User", "User", null, null, null, null, null, PersonClassification.PF.getType());
	private static final UserModel userMock = new UserModel(null, "root", "Root@10", new Classifier(1), null, personMock, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	
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
		
		classifierService.save( userTypeMock, profile);
		
		Optional<Person> dbPerson = this.personService.save(personMock, profile);
		personMock.setId( dbPerson.get().getId() );
		
		Optional<UserModel> dbUser = this.userService.save(userMock, profile);
		userMock.setId( dbUser.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.DocumentDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Document entity ) throws Exception {
		Optional<Document> entityId = dao.save( entity );
		
		assertNotNull( entityId );
		assertTrue( entityId.isPresent() );
		assertEquals( entity.getId(), entityId.get().getId() );
	}
	
	@Order(3)
	@DisplayName( "listAll - Quando listar todos as fontes" )
	@ParameterizedTest
	@MethodSource("com.portal.dao.DocumentDAOTest#whenListAllthenReturnEntityList")
	void whenListAll_ReturnListDocument( int page, int size, String sortDir, String sort, int validId ) throws Exception {
		
		PageRequest pageReq = PageRequest.of(page, size, Direction.fromString( sortDir ), sort);
		List<Document> list = dao.listAll( pageReq  );

		assertNotNull( list );
		assertEquals( size, list.size());
		
		Document dbDocument = list.get( 0 );
		
		assertNotNull( dbDocument );
		assertEquals( validId, dbDocument.getId());
	}
	
	@Test
	@Order(3)
	@DisplayName( "getById - Dado um ID existente retornar o documento" )
	void givenExistedDocument_whenGetById_ThenReturnDocument() throws Exception {
		
		Document mock = new Document(1, "File 1", "/tmp", "application/json", "DESCR 1", LocalDateTime.of(2022, 04, 15, 00, 00, 00, 00), new Classifier(82), userMock);

		Optional<Document> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getId(), entityDB.get().getId() );
		assertEquals( mock.getFileName(), entityDB.get().getFileName() );
		assertEquals( mock.getFilePath(), entityDB.get().getFilePath() );
		assertEquals( mock.getContentType(), entityDB.get().getContentType() );
		assertEquals( mock.getDescription(), entityDB.get().getDescription() );
		assertEquals( mock.getCreateDate(), entityDB.get().getCreateDate() );
		assertEquals( mock.getType(), entityDB.get().getType() );
		assertEquals( mock.getUser(), entityDB.get().getUser() );
	}
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.DocumentDAOTest#listEntityToFind")
	@DisplayName( "Quando busca retornar os IDs salvos" )
	void whenFind_ReturnProposal( Document mock, Document filter, int expectedSize ) throws Exception {
		List<Document> entityDB = dao.find( filter, null );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isEmpty() );
		assertEquals( expectedSize, entityDB.size() );
		
		Optional<Document> entity = entityDB.stream().filter(item->item.getId().equals( mock.getId() ) ).findFirst();
		
		assertEquals( mock, entity.get() );
		assertEquals( mock.getId(), entity.get().getId() );
		assertEquals( mock.getFileName(), entity.get().getFileName() );
		assertEquals( mock.getFilePath(), entity.get().getFilePath() );
		assertEquals( mock.getContentType(), entity.get().getContentType() );
		assertEquals( mock.getDescription(), entity.get().getDescription() );
		assertEquals( mock.getCreateDate(), entity.get().getCreateDate() );
		assertEquals( mock.getType(), entity.get().getType() );
		assertEquals( mock.getUser(), entity.get().getUser() );
	}
	
	
	@Order(3)
	@ParameterizedTest
	@MethodSource("com.portal.dao.DocumentDAOTest#listEntityToUpdate")
	@DisplayName( "update - Quando atualizar retorna o novo objeto" )
	void whenUpdate_CheckNewValues( Document mock ) throws Exception {
		dao.update( mock );
		
		Optional<Document> entityDB = dao.getById( mock.getId() );
		
		assertNotNull( entityDB );
		assertEquals( mock, entityDB.get() );
		assertEquals( mock.getId(), entityDB.get().getId() );
		assertEquals( mock.getFileName(), entityDB.get().getFileName() );
		assertEquals( mock.getFilePath(), entityDB.get().getFilePath() );
		assertEquals( mock.getContentType(), entityDB.get().getContentType() );
		assertEquals( mock.getDescription(), entityDB.get().getDescription() );
		assertEquals( mock.getCreateDate(), entityDB.get().getCreateDate() );
		assertEquals( mock.getType(), entityDB.get().getType() );
		assertEquals( mock.getUser(), entityDB.get().getUser() );
	}
	
	@Test
	@Order(4)
	@DisplayName( "delete - Quando deletado não pode mais existir" )
	void givenExistedDocument_whenDelete_ThenNoFind() throws Exception {
		int id = 2;
		
		dao.delete( id );
		
		Optional<Document> entityDB = dao.getById( id );
		
		assertNotNull( entityDB );
		assertFalse( entityDB.isPresent() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> whenListAllthenReturnEntityList() {
	    return Stream.of(
			Arguments.of(0, 3, "DESC", "doc_id", 3),
			Arguments.of(0, 1, "DESC", "doc_id", 3),
			Arguments.of(0, 1, "DESC", "create_date", 1),
			Arguments.of(0, 1, "DESC", "file_name", 3),
			Arguments.of(1, 1, "DESC", "doc_id", 2),
			Arguments.of(0, 1, "ASC", "doc_id", 1)
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Document(null, "File 1", "/tmp", "application/json", "DESCR 1", LocalDateTime.of(2022, 04, 15, 00, 00, 00, 00), new Classifier(82), userMock) ),
    		Arguments.of( new Document(null, "File 2", "/opt", "application/pdf", "DESCR 2", LocalDateTime.of(2022, 04, 10, 00, 00, 00, 00), new Classifier(83), userMock) ),
    		Arguments.of( new Document(null, "File 3", "/home", "application/text", "DESCR 3", LocalDateTime.of(2022, 04, 11, 00, 00, 00, 00), new Classifier(81), userMock) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Document(1, "File 1.1", "/tmp", "application/json", "DESCR 1", LocalDateTime.of(2022, 04, 20, 00, 00, 00, 00), new Classifier(83), userMock) ),
    		Arguments.of( new Document(2, "File 2.2", "/opt/a", "application/pdf", "DESCR 2", LocalDateTime.of(2022, 04, 10, 00, 00, 00, 00), new Classifier(83), userMock) ),
    		Arguments.of( new Document(3, "File 3.3", "/home", "application/doc", "DESCR 3.2", LocalDateTime.of(2022, 04, 11, 00, 00, 00, 00), new Classifier(81), userMock) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
		Document mock = new Document(1, "File 1", "/tmp", "application/json", "DESCR 1", LocalDateTime.of(2022, 04, 15, 00, 00, 00, 00), new Classifier(82), userMock);
		
	    return Stream.of(
    		Arguments.of( mock, Document.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().fileName( mock.getFileName() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().filePath( mock.getFilePath() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().contentType( mock.getContentType() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().description( mock.getDescription() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().createDate( mock.getCreateDate() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().type( mock.getType() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().user( mock.getUser() ).build(), 3 )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSearch() {
		Document mock = new Document(1, "File 1", "/tmp", "application/json", "DESCR 1", LocalDateTime.of(2022, 04, 15, 00, 00, 00, 00), new Classifier(82), userMock);
		
	    return Stream.of(
    		Arguments.of( mock, Document.builder().id( mock.getId() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().fileName( "File" ).build(), 3 ),
    		Arguments.of( mock, Document.builder().filePath( "/" ).build(), 3 ),
    		Arguments.of( mock, Document.builder().contentType( "application" ).build(), 3 ),
    		Arguments.of( mock, Document.builder().description( "DESC" ).build(), 3 ),
    		Arguments.of( mock, Document.builder().createDate( mock.getCreateDate() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().type( mock.getType() ).build(), 1 ),
    		Arguments.of( mock, Document.builder().user( mock.getUser() ).build(), 3 )
	    );
	}
	
}
