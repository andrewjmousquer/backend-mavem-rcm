package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PartnerPerson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PartnerPersonDTO {

	private PersonDTO person;
	
	private PartnerDTO partner;

	private ClassifierDTO personType;
	
	private List<PartnerPersonCommissionDTO> commissionList;

	public static PartnerPersonDTO toDTO( PartnerPerson model ) {
		if( model == null ) {
			return null;
		}
		
		return PartnerPersonDTO.builder()
							.person(PersonDTO.toDTO(model.getPerson()))
							.partner(PartnerDTO.toDTO(model.getPartner()))
							.personType( ClassifierDTO.toDto( model.getPersonType() ) )
							.commissionList(PartnerPersonCommissionDTO.toDTO(model.getCommissionList()))
							.build();
	}

	public static List<PartnerPersonDTO> toDTO( List<PartnerPerson> models ) {
		if( models == null ) {
			return null;
		}
		
		return models.stream().map( PartnerPersonDTO::toDTO ).collect( Collectors.toList() );	
	}
}