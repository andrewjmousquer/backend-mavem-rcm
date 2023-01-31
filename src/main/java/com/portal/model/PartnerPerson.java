package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.portal.dto.PartnerPersonDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PartnerPerson {
	
	@EqualsAndHashCode.Include
	@NotNull
	private Person person;
	
	@EqualsAndHashCode.Include
	@NotNull
	private Partner partner;

	@NotNull
	private Classifier personType;
	
	private List<PartnerPersonCommission> commissionList;
	
	public PartnerPerson(Partner partner) {
		this.partner = partner;
	}
	
	public PartnerPerson(Person person) {
		this.person = person;
	}

	public static PartnerPerson toEntity( PartnerPersonDTO model ) {
		if( model == null ) {
			return null;
		}
		
		return PartnerPerson.builder()
							.person(Person.toEntity(model.getPerson()))
							.partner(Partner.toEntity(model.getPartner()))
							.personType( Classifier.toEntity(model.getPersonType()) )
							.commissionList(PartnerPersonCommission.toEntity(model.getCommissionList()))
							.build();
	}

	public static List<PartnerPerson> toEntity( List<PartnerPersonDTO> models ) {
		if( models == null ) {
			return null;
		}
		
		return models.stream().map( PartnerPerson::toEntity ).collect( Collectors.toList() );	
	}
}
