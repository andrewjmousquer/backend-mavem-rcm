package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.ItemType;

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
public class ItemTypeDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private Boolean mandatory;
	
	private Boolean multi;
	
	private Integer seq;

	private Integer responsability_cla_id;

	private String term;

	private Boolean term_work_day;

	private Boolean highlight;
	
	public static ItemTypeDTO toDTO( ItemType model ) {
		
		if( model == null ) {
			return null;
		}
		
		return ItemTypeDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.mandatory( model.getMandatory() )
							.multi( model.getMulti() )
							.seq( model.getSeq() )
							.build();
	}

	public static List<ItemTypeDTO> toDTO( List<ItemType> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( ItemTypeDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
