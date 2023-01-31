package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Source;

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
public class SourceDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private Boolean active;
	
	public static SourceDTO toDTO( Source model ) {
		
		if( model == null ) {
			return null;
		}
		
		return SourceDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.active( model.getActive() )
							.build();
	}

	public static List<SourceDTO> toDTO( List<Source> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( SourceDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
