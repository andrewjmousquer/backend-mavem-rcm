package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Channel;

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
public class ChannelDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private Boolean active;
	
	private Boolean hasPartner;
	
	private Boolean hasInternalSale;
	
	public static ChannelDTO toDTO( Channel model ) {
		
		if( model == null ) {
			return null;
		}
		
		return ChannelDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.active( model.getActive() )
							.hasPartner( model.getHasPartner() )
							.hasInternalSale( model.getHasInternalSale() )
							.build();
	}

	public static List<ChannelDTO> toDTO( List<Channel> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( ChannelDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
