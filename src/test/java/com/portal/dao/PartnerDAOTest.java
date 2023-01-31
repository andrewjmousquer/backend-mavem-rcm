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

import com.portal.dao.impl.PartnerDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.PersonClassification;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.PartnerGroup;
import com.portal.model.Person;
import com.portal.model.UserModel;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PartnerGroupService;
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
class PartnerDAOTest {

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
	private PartnerDAO dao;
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private ClassifierService classifierService;
	
	@Autowired
	private PartnerGroupService partnerGroupService;
	
	@Autowired
	private ChannelService channelService;
	
	private static final Channel channel = new Channel(null, "Channel 1", true, true, true);
	private static final Channel channel2 = new Channel(null, "Channel 2", true, true, true);
	private static final Person person = new Person(null, "Person 1", "Job 1", "00000000001", "00000000000001", "00000000001", "00000000000000000001", null, PersonClassification.PF.getType() );
	private static final Person person2 = new Person(null, "Person 2",  null, null, null, null, null, null, PersonClassification.PJ.getType() );
	private static final PartnerGroup partnerGroup = new PartnerGroup(null, "GROUP 1", true);
	
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
		
		Optional<PartnerGroup> dbPartnerGroup = partnerGroupService.save(  partnerGroup, profile );
		partnerGroup.setId( dbPartnerGroup.get().getId() );
		Optional<Person> dbPerson = personService.save( person, profile );
		person.setId( dbPerson.get().getId() );
		Optional<Person> dbPerson2 = personService.save( person2 , profile );
		person2.setId( dbPerson2.get().getId() );
		Optional<Channel> dbChannel = channelService.save(channel, profile);
		channel.setId( dbChannel.get().getId() );
		Optional<Channel> dbChannel2 = channelService.save(channel2, profile);
		channel2.setId( dbChannel2.get().getId() );
	}
	
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerDAOTest#listEntityToSave")
	@DisplayName( "Quando salvar retornar os IDs salvos" )
	void whenSave_ReturnSavedId( Partner model ) throws Exception {
		Optional<Partner> modelDB = dao.save( model );
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( model.getId(), modelDB.get().getId() );
	}
	
	@Test
	@Order(3)
	void whenListAll_ReturnList() throws Exception {
		PageRequest pageReq = PageRequest.of(0, 100, Sort.Direction.ASC, "ptn_id");
		List<Partner> list = dao.listAll( pageReq  );

		assertEquals(listEntityToSave().count(), list.size());
	}
	
	@Test
	@Order(3)
	void givenExistedPartner_whenGetById_ThenReturn() throws Exception {
		Partner model = Partner.builder()
								.id( 1 )
								.build();

		Optional<Partner> modelDB = dao.getById( model.getId() );
		
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( model, modelDB.get() );
	}

	@ParameterizedTest
	@Order(4)
	@MethodSource("com.portal.dao.PartnerDAOTest#listEntityToFind")
	void whenFind_ThenReturn( Partner toFind, Partner mockCheck, int expectedSize ) throws Exception {
		
		List<Partner> modelDB = dao.find( toFind, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( expectedSize, modelDB.size() );
		
		Optional<Partner> entity = modelDB.stream().filter(item->item.getId().equals( mockCheck.getId() ) ).findFirst();
		
		assertEquals( mockCheck.getId(), entity.get().getId() );
		assertEquals( mockCheck.getSituation(), entity.get().getSituation() );
		
		assertNotNull( entity.get().getChannel() );
		assertEquals( mockCheck.getChannel(), entity.get().getChannel() );
		assertEquals( mockCheck.getChannel().getName(), entity.get().getChannel().getName() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mockCheck.getPerson(), entity.get().getPerson() );
		assertEquals( mockCheck.getPerson().getName(), entity.get().getPerson().getName() );
		assertNotNull( entity.get().getPartnerGroup() );
		assertEquals( mockCheck.getPartnerGroup(), entity.get().getPartnerGroup() );
	}
	
	@ParameterizedTest
	@Order(4)
	@MethodSource("com.portal.dao.PartnerDAOTest#listEntityToSearch")
	void whenSearch_ThenReturn( Partner toSearch, Partner mockCheck, int expectedSize ) throws Exception {
		List<Partner> modelDB = dao.search( toSearch, null );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isEmpty() );
		assertEquals( expectedSize, modelDB.size() );
		
		Optional<Partner> entity = modelDB.stream().filter(item->item.getId().equals( mockCheck.getId() ) ).findFirst();
		
		assertEquals( mockCheck.getId(), entity.get().getId() );
		assertEquals( mockCheck.getSituation(), entity.get().getSituation() );
		
		assertNotNull( entity.get().getChannel() );
		assertEquals( mockCheck.getChannel(), entity.get().getChannel() );
		assertEquals( mockCheck.getChannel().getName(), entity.get().getChannel().getName() );
		
		assertNotNull( entity.get().getPerson() );
		assertEquals( mockCheck.getPerson(), entity.get().getPerson() );
		assertEquals( mockCheck.getPerson().getName(), entity.get().getPerson().getName() );
		assertNotNull( entity.get().getPartnerGroup() );
		assertEquals( mockCheck.getPartnerGroup(), entity.get().getPartnerGroup() );
	}
	
	@Order(5)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerDAOTest#listEntityToUpdate")
	void whenUpdate_CheckNewValues( Partner partner ) throws Exception {
		dao.update( partner );
		
		Optional<Partner> modelDB = dao.getById( partner.getId() );
		
		assertNotNull( modelDB );
		assertTrue( modelDB.isPresent() );
		assertEquals( partner.getId(), modelDB.get().getId() );
		assertEquals( partner.getSituation(), modelDB.get().getSituation() );
		assertNotNull( modelDB.get().getChannel() );
		assertEquals( partner.getChannel(), modelDB.get().getChannel() );
		assertNotNull( modelDB.get().getPerson() );
		assertEquals( partner.getPerson(), modelDB.get().getPerson() );
		assertEquals( partner.getPartnerGroup(), modelDB.get().getPartnerGroup() );
	}
	
	@Test
	@Order(6)
	void givenExistedBrand_whenDelete_ThenNoFind() throws Exception {
		dao.delete( 1 );
		
		Optional<Partner> modelDB = dao.getById( 1 );
		
		assertNotNull( modelDB );
		assertFalse( modelDB.isPresent() );
	}
	
	private static Stream<Arguments> listEntityToSave() {
	    return Stream.of(
    		Arguments.of( new Partner(null, new Classifier(210), channel, person, partnerGroup)),
    		Arguments.of( new Partner(null, new Classifier(210), channel2, person2, null))
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToUpdate() {
	    return Stream.of(
    		Arguments.of( new Partner(1, new Classifier(210), channel, person, null) ),
    		Arguments.of( new Partner(1, new Classifier(210), channel, person2, null) ),
    		Arguments.of( new Partner(1, new Classifier(210), channel2, person2, null) )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToFind() {
		Partner mockCheck = new Partner(1, new Classifier(210), channel, person, partnerGroup);
		
	    return Stream.of(
    		Arguments.of( Partner.builder().id( 1 ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().situation( new Classifier(210) ).build(), mockCheck, 2 ),
    		Arguments.of( Partner.builder().id( 1 ).situation( new Classifier(210) ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().id( person.getId() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().name( person.getName() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().cnpj( person.getCnpj() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().rne( person.getRne() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().rg( person.getRg() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().cpf( person.getCpf() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().channel( Channel.builder().id( channel.getId() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().channel( Channel.builder().name( channel.getName() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().partnerGroup( PartnerGroup.builder().id( partnerGroup.getId() ).build() ).build(), mockCheck, 1 )
	    );
	}
	
	@SuppressWarnings("unused")
	private static Stream<Arguments> listEntityToSearch() {
		Partner mockCheck = new Partner(1, new Classifier(210), channel, person, partnerGroup);
		
	    return Stream.of(
    		Arguments.of( Partner.builder().id( 1 ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().situation( new Classifier(210) ).build(), mockCheck, 2 ),
    		Arguments.of( Partner.builder().id( 1 ).situation( new Classifier(210) ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().id( person.getId() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().name( "Person" ).build() ).build(), mockCheck, 2 ),
    		Arguments.of( Partner.builder().person( Person.builder().cnpj( "000" ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().rne( "000" ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().rg( "000" ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().person( Person.builder().cpf( "000" ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().channel( Channel.builder().id( channel.getId() ).build() ).build(), mockCheck, 1 ),
    		Arguments.of( Partner.builder().channel( Channel.builder().name( "Channel" ).build() ).build(), mockCheck, 2 ),
    		Arguments.of( Partner.builder().partnerGroup( PartnerGroup.builder().id( partnerGroup.getId() ).build() ).build(), mockCheck, 1 )
	    );
	}
}
