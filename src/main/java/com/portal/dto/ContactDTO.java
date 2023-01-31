package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.model.Classifier;
import com.portal.model.Contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContactDTO{

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String complement;
	
	private String value;
	
	private Classifier type;
	
	@JsonBackReference
	private PersonDTO person;
	
    public static ContactDTO toDTO(Contact contact) {
        if (contact == null) {
            return null;
        }
        
        // Removendo a referência circular
        if( contact.getPerson() != null ) contact.getPerson().setContacts(null);

        return ContactDTO.builder()
			                .id(contact.getId())
			                .complement(contact.getComplement())
			                .value(contact.getValue())
			                .type(contact.getType())
			                .person(PersonDTO.toDTO(contact.getPerson()))
			                .build();
    }

	public static List<ContactDTO> toDTO(List<Contact> contacts) {
        if (contacts == null) {
            return null;
        }

        return contacts.stream().map(ContactDTO::toDTO).collect(Collectors.toList());

    }

}