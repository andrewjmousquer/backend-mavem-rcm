package com.portal.unit;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dto.ContactDTO;
import com.portal.dto.PersonDTO;
import com.portal.model.Contact;
import com.portal.model.Person;

@ExtendWith(SpringExtension.class)
class PersonTest {
	
	@Test
	void givenPersonDTO_whenConvertToEntity_thenReturnPerson() throws JsonProcessingException {
		
		PersonDTO dto = PersonDTO.builder()
									.id( 1 )
									.name( "Person 1" )
									.build();
		
		ContactDTO c1 = ContactDTO.builder()
										.id( 1 )
										.value( "C1" )
										.person( dto )
										.build();

		dto.setContacts( Arrays.asList( c1 ) );
		
		Person entity = Person.toEntity( dto ); 
		
		ObjectMapper mapper = new ObjectMapper();
		System.out.println( "\n\n" + mapper.writeValueAsString( entity ) + "\n\n");
	}
	
	@Test
	void givenPerson_whenConvertToDTO_thenReturnPerson() throws JsonProcessingException {
		
		PersonDTO dto2 = PersonDTO.builder()
								.id( 2 )
								.name( "Person 2" )
								.build();
		
		Person dto = Person.builder()
								.id( 1 )
								.name( "Person 1" )
								.build();
		
		Contact c1 = Contact.builder()
								.id( 1 )
								.value( "C1" )
								.person( dto )
								.build();
		
		Contact c2 = Contact.builder()
								.id( 2 )
								.value( "C2" )
								.person( dto )
								.build();
		
		dto.setContacts( Arrays.asList( c1, c2 ) );
		
		
		ContactDTO c3 = ContactDTO.builder()
										.id( 2 )
										.value( "C2" )
										.person( dto2 )
										.build();
		Contact.toEntity( c3 );
		
//		ObjectMapper mapper = new ObjectMapper();
//		System.out.println( "\n\n" + mapper.writeValueAsString( dto ) + "\n\n");
	}
}
