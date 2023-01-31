package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PartnerGroup;

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
public class PartnerGroupDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private Boolean active;
	
	public static PartnerGroupDTO toDTO( PartnerGroup model ) {
		
		if( model == null ) {
			return null;
		}
		
		return PartnerGroupDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.active( model.getActive() )
							.build();
	}

	public static List<PartnerGroupDTO> toDTO( List<PartnerGroup> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( PartnerGroupDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
