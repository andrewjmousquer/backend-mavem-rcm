package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Classifier;
import com.portal.model.Partner;

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
public class PartnerDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private PersonDTO person;
	
	private Classifier situation;
	
	private Integer additionalTerm;
	
	private ChannelDTO channel;
		
	private PartnerGroupDTO partnerGroup; 
	
	private Boolean isAssistance;
		
	private List<BrandDTO> brandList;
	
	private List<PartnerPersonDTO> employeeList;

	private List<SellerDTO> sellerList;

	public static PartnerDTO toDTO( Partner model ) {
		if( model == null ) {
			return null;
		}
		
		return PartnerDTO.builder()
							.id( model.getId() )
							.person( PersonDTO.toDTO(model.getPerson()) )
							.situation( model.getSituation() )
							.additionalTerm( model.getAdditionalTerm() )
							.channel( ChannelDTO.toDTO( model.getChannel() ) )
							.partnerGroup( PartnerGroupDTO.toDTO( model.getPartnerGroup() ) )
							.brandList( BrandDTO.toDTO( model.getBrandList() ) )
							.employeeList( PartnerPersonDTO.toDTO(model.getEmployeeList() ))
							.sellerList ( SellerDTO.toDTO( model.getSellerList() ) )
							.isAssistance (model.isAssistance())
							.build();
	}

	public static List<PartnerDTO> toDTO( List<Partner> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( PartnerDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
