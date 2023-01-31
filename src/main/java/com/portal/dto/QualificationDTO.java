package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Qualification;

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
public class QualificationDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private Integer seq;
	
	private Integer parentId;
	
	private Boolean active;
	
	private Boolean required;
	
	public static QualificationDTO toDTO( Qualification model ) {
		
		if( model == null ) {
			return null;
		}
		
		return QualificationDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.seq( model.getSeq() )
							.parentId( model.getParentId() )
							.required( model.getRequired() )
							.active( model.getActive() )
							.build();
	}

	public static List<QualificationDTO> toDTO( List<Qualification> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( QualificationDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
