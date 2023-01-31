package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.dto.ContactDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

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
public class Contact {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@Size(max = 150, groups = {OnUpdate.class, OnSave.class})
	private String complement;
	
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 150, groups = {OnUpdate.class, OnSave.class})
	private String value;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier type;
	
	@JsonBackReference
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Person person;

	public static Contact toEntity( ContactDTO contactDto ) {
		
        if (contactDto == null) {
            return null;
        }
        
        // Removendo a referência circular
        if( contactDto.getPerson() != null ) contactDto.getPerson().setContacts(null);
        
        return Contact.builder()
			                .id(contactDto.getId())
			                .complement(contactDto.getComplement())
			                .value(contactDto.getValue())
			                .type(contactDto.getType())
			                .person( Person.toEntity( contactDto.getPerson() ) )
			                .build();
    }

	public static List<Contact> toEntity(List<ContactDTO> contactsDto) {
        if (contactsDto == null) {
            return null;
        }

        return contactsDto.stream().map( Contact::toEntity ).collect( Collectors.toList() );
    }

}