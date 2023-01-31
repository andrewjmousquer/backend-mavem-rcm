package com.portal.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PriceList;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode( onlyExplicitlyIncluded = true )
public class PriceListDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	private String name;
	private LocalDateTime start;
	private LocalDateTime end;
	private ChannelDTO channel;
	private Boolean allPartners;
	
	public static PriceListDTO toDTO( PriceList model ) {
		
		if( model == null ) {
			return null;
		}
		
		return PriceListDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.start( model.getStart() )
							.end( model.getEnd() )
							.channel( ChannelDTO.toDTO( model.getChannel() ) )
							.allPartners( model.getAllPartners() )
							.build();
	}
	
	public static List<PriceListDTO> toDTO( List<PriceList> models ) {
		
		if( models == null ) {
			return new ArrayList<>();
		}
		
		return models.stream()
						.map( PriceListDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
