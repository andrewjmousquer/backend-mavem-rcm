package com.portal.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import com.portal.dao.impl.PartnerBrandDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.AppException;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.PartnerGroup;
import com.portal.model.Person;
import com.portal.model.UserModel;
import com.portal.service.imp.BrandService;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PartnerService;
import com.portal.service.imp.PersonService;

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
class PartnerBrandDAOTest {

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
	private PartnerBrandDAO dao;
	
	@Autowired
	private BrandService brandService;
	
	@Autowired
	private PartnerService partnerService;
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private ClassifierService classifierService;

	@Autowired
	private ChannelService channelService;
	
	private static Channel channel = new Channel( null, "Channel 1", true, true, true);
	private static Brand brand1 = new Brand( null, "BRAND 1", true );
	private static Brand brand2 = new Brand( null, "BRAND 2", true );
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

		Optional<Brand> brd = brandService.save( brand1, profile);
		brand1 = brd.get();
		brd = brandService.save( brand2, profile);
		brand2 = brd.get();
		
		Optional<Channel> chn = channelService.save( channel, profile);
		channel = chn.get();

		classifierService.save( PersonClassification.ESTRANGEIRO.getType(), profile);
		classifierService.save( PersonClassification.PF.getType(), profile);
		classifierService.save( PersonClassification.PJ.getType(), profile);

		Optional<Person> per = personService.save( person , profile );
		person = per.get();

		partnerService.save( new Partner(null, new Classifier(210), channel, person, group), profile);
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerBrandDAOTest#listEntityToSave")
	@DisplayName( "save - Quando salva o relacionamento então não retorna erro" )
	void whenSave_ThenNoError( Integer brdId, Integer ptnId ) throws Exception {
		assertDoesNotThrow(()->dao.save( brdId, ptnId ));
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByBrand - Dado uma pessoa lista todos os parceiros relacionados" )
	void givenBrand_whenFindByBrand_ReturnListPartner() throws Exception {
		List<Partner> list = dao.findByBrand( 1 );

		assertNotNull( list );
		assertFalse( list.isEmpty() );
		assertEquals( 1, list.size());
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByBrand - Dado o Id da marca como zero deve retornar erro " )
	void givenZeroBrandID_whenFindByBrand_ReturnError() throws Exception {
		AppException ex = assertThrows( AppException.class, ()->dao.findByBrand( 0 ) );
		assertEquals( ex.getMessage(), "O ID da marca está inválido.");
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByBrand - Dado o Id da marca como nulo deve retornar erro " )
	void givenNullBrandID_whenFindByBrand_ReturnError() throws Exception {
		AppException ex = assertThrows( AppException.class, ()->dao.findByBrand( null ) );
		assertEquals( ex.getMessage(), "O ID da marca está inválido.");
	}
	
	@Test
	@Order(3)
	@DisplayName( "findByPartner - Dado um parceiro lista todos as marcas relacionadas" )
	void givenPartner_whenFindBypartner_ReturnListBrand() throws Exception {
		List<Brand> list = dao.findByPartner( 1 );

		assertNotNull( list );
		assertFalse( list.isEmpty() );
		assertEquals( 2, list.size());
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
	@DisplayName( "getPartner - Dado um parceiro e uma marca retonar o parceiro relacionado" )
	void givenPartnerAndBrand_whenGetPartner_ReturnPartner() throws Exception {
		Partner mock = new Partner(1, new Classifier(210), channel, person, group);
		Optional<Partner> partner = partnerService.getById(1);

		assertNotNull( partner );
		assertEquals( mock, partner.get());
		assertEquals( mock.getChannel(), partner.get().getChannel());
		assertEquals( mock.getPartnerGroup(), partner.get().getPartnerGroup());
		assertEquals( mock.getPerson(), partner.get().getPerson());
		assertEquals( mock.getSituation(), partner.get().getSituation());
	}
	
	@Test
	@Order(3)
	@DisplayName( "getBrand - Dado um parceiro e uma pessoa retonar a pessoa relacionado" )
	void givenPartnerAndBrand_whenGetBrand_ReturnBrand() throws Exception {
		Brand mock = brand1;
		Optional<Brand> brand = brandService.getById( 1 );

		assertNotNull( brand );
		assertEquals( mock, brand.get());
		assertEquals( mock.getName(), brand.get().getName());
		assertEquals( mock.getActive(), brand.get().getActive());
	}
	
	@Test
	@Order(4)
	@DisplayName( "save - Quando salva um relacionamento duplicado então atualiza e não da erro" )
	void whenSaveDuplicate_ThenNoError() throws Exception {
		assertDoesNotThrow(()->dao.save( 1, 1 ));
	}
	
	@Order(5)
	@DisplayName( "delete - Dado um parceiro e uma marca quando deletamos o mesmo deve sumir" )
	void givenBrandAndPartner_whenDelete_ThenNoRecordFound() throws Exception {
		assertDoesNotThrow(()->dao.delete( 2, null) );
		Optional<Brand> person = brandService.getById( 2 );
		assertFalse( person.isPresent() );
	}
	
	
	@Order(6)
	@DisplayName( "deleteByPartner - Dada um parceiro quando excluida deve sumir os relacionamentos. " )
	void givenPartner_whenDelete_ThenNoRecordFound() throws Exception {
		assertDoesNotThrow( ()->dao.delete( 1, null ));
		List<Brand> list = dao.findByPartner( 1 );
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
