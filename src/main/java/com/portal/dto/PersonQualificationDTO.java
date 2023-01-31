package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.model.PersonQualification;

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
public class PersonQualificationDTO {

	@JsonBackReference
	private PersonDTO person;
	
	private QualificationDTO qualification;

	private String comments;
  
    public static PersonQualificationDTO toDTO(PersonQualification personQualification) {
        if (personQualification == null) {
            return null;
        }

        return PersonQualificationDTO.builder()
        		.person(PersonDTO.toDTO(personQualification.getPerson()))
        		.qualification(QualificationDTO.toDTO(personQualification.getQualification()))
                .comments(personQualification.getComments())
                .build();
    }

    public static List<PersonQualificationDTO> toDTO(List<PersonQualification> personsQualification) {
        if (personsQualification == null) {
            return null;
        }
        return personsQualification.stream().map(PersonQualificationDTO::toDTO).collect(Collectors.toList());
    }
}
