package com.portal.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.dto.PersonRelatedDTO;
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
public class PersonRelated {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;

	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	private LocalDate birthdate;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier relatedType;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@JsonBackReference
	private Person person;
	
	public static PersonRelated toEntity(PersonRelatedDTO personDTO) {
		if (personDTO == null) {
			return null;
		}

		return PersonRelated.builder()
				.id(personDTO.getId())
				.name(personDTO.getName())
				.birthdate(personDTO.getBirthdate())
				.relatedType(personDTO.getRelatedType())
				.person(Person.toEntity(personDTO.getPerson()))
				.build();
	}

	public static List<PersonRelated> toEntity(List<PersonRelatedDTO> list) {
		if (list == null) {
			return null;
		}
		return list.stream().map(PersonRelated::toEntity).collect(Collectors.toList());
	}

}