package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import com.portal.dao.impl.PriceListPartnerDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.AppException;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.PartnerGroup;
import com.portal.model.Person;
import com.portal.model.PriceList;
import com.portal.model.UserModel;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PartnerService;
import com.portal.service.imp.PersonService;
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
class PriceListPartnerDAOTest {

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
	private PriceListPartnerDAO dao;
	
	@Autowired
	private PartnerService partnerService;
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private ClassifierService classifierService;

	@Autowired
	private ChannelService channelService;

	@Autowired
	private PriceListService priceListService;
	
	private static Channel channel = new Channel( null, "Channel 1", true, true, true);
	private static Person person = new Person( null, "Person PJ", "Concessionária", null, "00000000000001", null, null, null, PersonClassification.PJ.getType() );
	private static PartnerGroup group = null;
	
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
		
		Optional<Channel> chn = channelService.save( channel, profile);
		channel = chn.get();
		
		Optional<Person> per = personService.save( person , profile );
		person = per.get();

		partnerService.save( new Partner(null, new Classifier(210), channel, person, group), profile);
		
		priceListService.save( new PriceList(null, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false) , profile);
		priceListService.save( new PriceList(null, "PriceList 2", LocalDateTime.of(2022, 02, 01, 00, 00, 00, 00), LocalDateTime.of(2022, 02, 10, 00, 00, 00, 00), channel, false) , profile);
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PriceListPartnerDAOTest#listEntityToSave")
	@DisplayName( "save - Quando salva o relacionamento então não retorna erro" )
	void whenSave_ThenNoError( Integer prlId, Integer ptnId ) throws Exception {
		assertDoesNotThrow(()->dao.save( prlId, ptnId ));
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByPriceList - Dado uma lista de preço lista todos os parceiros relacionados" )
	void givenPriceList_whenFindByPriceList_ReturnListPartner() throws Exception {
		List<Partner> list = dao.findByPriceList( 1 );

		assertNotNull( list );
		assertFalse( list.isEmpty() );
		assertEquals( 1, list.size());
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByPartner - Dado um parceiro lista todos as listas de preços relacionadas" )
	void givenPartner_whenFindBypartner_ReturnPriceList() throws Exception {
		List<PriceList> list = dao.findByPartner( 1 );

		assertNotNull( list );
		assertFalse( list.isEmpty() );
		assertEquals( 2, list.size());
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByPriceList - Dado o Id da lista de preço como zero deve retornar erro " )
	void givenZeroPriceLisID_whenFindByPriceList_ReturnError() throws Exception {
		AppException ex = assertThrows( AppException.class, ()->dao.findByPriceList( 0 ) );
		assertEquals( ex.getMessage(), "O ID da lista de preço está inválido.");
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByPriceList - Dado o Id da lista de preço como nulo deve retornar erro " )
	void givenNullPriceListID_whenFindByPriceList_ReturnError() throws Exception {
		AppException ex = assertThrows( AppException.class, ()->dao.findByPriceList( null ) );
		assertEquals( ex.getMessage(), "O ID da lista de preço está inválido.");
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByPartner - Dado o Id do parceiro como zero deve retornar erro " )
	void givenZeroPartnerID_whenFindByPartner_ReturnError() throws Exception {
		AppException ex = assertThrows( AppException.class, ()->dao.findByPartner( 0 ) );
		assertEquals( ex.getMessage(), "ID do parceiro está inválido.");
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByPartner - Dado o Id do parceiro como nulo deve retornar erro " )
	void givenNullPartnerID_whenFindByPartner_ReturnError() throws Exception {
		AppException ex = assertThrows( AppException.class, ()->dao.findByPartner( null ) );
		assertEquals( ex.getMessage(), "ID do parceiro está inválido.");
	}
	
	@Test
	@Order(3)
	@DisplayName( "getPartner - Dado um parceiro e uma lista de preço retonar o parceiro relacionado" )
	void givenPartnerAndPriceList_whenGetPartner_ReturnPartner() throws Exception {
		Partner mock = new Partner(1, new Classifier(210), channel, person, group);
		Optional<Partner> partner = dao.getPartner( 1, 1 );

		assertNotNull( partner );
		assertEquals( mock, partner.get());
		assertEquals( mock.getChannel(), partner.get().getChannel());
		assertEquals( mock.getPartnerGroup(), partner.get().getPartnerGroup());
		assertEquals( mock.getPerson(), partner.get().getPerson());
		assertEquals( mock.getSituation(), partner.get().getSituation());
	}
	
	@Test
	@Order(3)
	@DisplayName( "getPriceList - Dado um parceiro e uma lista de preço retonar a list de preço relacionado" )
	void givenPartnerAndPriceList_whenGetPriceList_ReturnPriceList() throws Exception {
		PriceList mock = new PriceList(1, "PriceList 1", LocalDateTime.of(2021, 12, 10, 00, 00, 00, 00), LocalDateTime.of(2021, 12, 31, 00, 00, 00, 00), channel, false);
		Optional<PriceList> entityDB = dao.getPriceList( 1, 1 );

		assertNotNull( entityDB );
		assertTrue( entityDB.isPresent() );
		assertEquals( mock, entityDB.get() );
		assertEquals( entityDB.get().getName(), mock.getName());
		assertEquals( entityDB.get().getStart(), mock.getStart());
		assertEquals( entityDB.get().getEnd(), mock.getEnd());
		assertNotNull( entityDB.get().getChannel() );
		assertEquals( entityDB.get().getChannel(), mock.getChannel());
		assertEquals( entityDB.get().getChannel().getName(), mock.getChannel().getName());
		assertEquals( entityDB.get().getChannel().getActive(), mock.getChannel().getActive());
	}
	
	@Test
	@Order(4)
	@DisplayName( "save - Quando salva um relacionamento duplicado então atualiza e não da erro" )
	void whenSaveDuplicate_ThenNoError() throws Exception {
		assertDoesNotThrow(()->dao.save( 1, 1 ));
	}
	
	@Order(5)
	@DisplayName( "delete - Dado um parceiro e uma lista de preço quando deletamos o mesmo deve sumir" )
	void givenPriceListAndPartner_whenDelete_ThenNoRecordFound() throws Exception {
		assertDoesNotThrow(()->dao.delete( 2, 1) );
		Optional<PriceList> entityDB = dao.getPriceList( 2, 1 );
		assertFalse( entityDB.isPresent() );
	}
	
	
	@Order(6)
	@DisplayName( "deleteByPriceList - Dada uma lista de preço quando excluida deve sumir os relacionamentos. " )
	void givenPartner_whenDelete_ThenNoRecordFound() throws Exception {
		assertDoesNotThrow( ()->dao.deleteByPriceList( 1 ));
		List<Partner> list = dao.findByPriceList( 1 );
		assertNotNull( list );
		assertTrue( list.isEmpty() );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( 1, 1 ),
    		Arguments.of( 2, 1 )
	    );
	}
}
