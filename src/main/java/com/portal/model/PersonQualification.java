package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.dto.PersonQualificationDTO;

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
public class PersonQualification {
	
	@EqualsAndHashCode.Include
	@NotNull
	@JsonBackReference
	private Person person;
	
	@EqualsAndHashCode.Include
	@NotNull
	private Qualification qualification;

	private String comments;
	
	public static PersonQualification toEntity(PersonQualificationDTO personQualificationDto) {
		if (personQualificationDto == null) {
			return null;
		}

		return PersonQualification.builder()
				.person(Person.toEntity(personQualificationDto.getPerson()))
				.qualification(Qualification.toEntity(personQualificationDto.getQualification()))
				.comments(personQualificationDto.getComments())
				.build();
	}

	public static List<PersonQualification> toEntity(List<PersonQualificationDTO> list) {
		if (list == null) {
			return null;
		}
		return list.stream().map(PersonQualification::toEntity).collect(Collectors.toList());
	}
}