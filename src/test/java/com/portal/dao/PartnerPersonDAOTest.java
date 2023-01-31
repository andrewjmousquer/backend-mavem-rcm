package com.portal.dao;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.portal.dto.UserProfileDTO;
import com.portal.enums.PersonClassification;
import com.portal.enums.PersonType;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.model.UserModel;
import com.portal.service.imp.ChannelService;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PartnerPersonService;
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
class PartnerPersonDAOTest {

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
	private PartnerPersonService service;
	
	@Autowired
	private PartnerService partnerService;
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private ClassifierService classifierService;

	@Autowired
	private ChannelService channelService;
	
	private static Channel channel = new Channel( null, "Channel 1", true, true, true);
	private static Person entity = new Person( null, "Partner 1", null, null, "00000000000014", null, null, null, PersonClassification.PJ.getType() );
	private static Person employee = new Person( null, "Person PF", "Vendedor", "00000000001", null, null, null, null, PersonClassification.PF.getType() );
	private static Person employee2 = new Person( null, "Person PF", "Gerente", "00000000002", null, null, null, null, PersonClassification.PF.getType() );
	private static Partner partner = new Partner( null, new Classifier(210), channel, entity, null);
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
		
		for( PersonType classifiers : PersonType.values() ) {
			classifierService.save( classifiers.getType(), profile);
		}
		
		Optional<Channel> dbChannel = channelService.save( channel, profile);
		channel.setId( dbChannel.get().getId() );
		Optional<Person> dbEntity = personService.save( entity, profile );
		entity.setId( dbEntity.get().getId() );
		Optional<Person> dbEmployee = personService.save( employee, profile );
		employee.setId( dbEmployee.get().getId() );
		Optional<Person> dbEmployee2 = personService.save( employee2, profile );
		employee2.setId( dbEmployee2.get().getId() );
		Optional<Partner> dbPartner = partnerService.save( partner, profile);
		partner.setId( dbPartner.get().getId() );
	}
	
	@Order(2)
	@ParameterizedTest
	@MethodSource("com.portal.dao.PartnerPersonDAOTest#listEntityToSave")
	@DisplayName( "save - Quando salva o relacionamento então não retorna erro" )
	void whenSave_ThenNoError( Integer ptnId, Integer perId) throws Exception {
		
	}
	

}
