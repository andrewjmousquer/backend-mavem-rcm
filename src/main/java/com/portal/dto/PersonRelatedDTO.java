package com.portal.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.model.Classifier;
import com.portal.model.PersonRelated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PersonRelatedDTO {

	@EqualsAndHashCode.Include
	private Integer id;

	private String name;
	
	private LocalDate birthdate;

	private Classifier relatedType;
	
	@JsonBackReference
	private PersonDTO person;
	
   public static PersonRelatedDTO toDTO(PersonRelated personRelated) {
        if (personRelated == null) {
            return null;
        }

        return PersonRelatedDTO.builder()
                .id(personRelated.getId())
                .name(personRelated.getName())
                .birthdate(personRelated.getBirthdate())
                .relatedType(personRelated.getRelatedType())
                .person(PersonDTO.toDTO(personRelated.getPerson()))
                .build();
    }

    public static List<PersonRelatedDTO> toDTO(List<PersonRelated> persons) {
        if (persons == null) {
            return null;
        }
        return persons.stream().map(PersonRelatedDTO::toDTO).collect(Collectors.toList());
    }
	
}
