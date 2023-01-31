package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemModel;

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
public class ItemDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private String cod;
	
	private Integer seq;
	
	private Boolean forFree;
	
	private Boolean generic;
	
	private Classifier mandatory;
	
	private ItemTypeDTO itemType;

	private String file;
	
	private String icon;
	
	private String description;
	
	private String hyperlink;

	private Classifier responsability;

	private Integer term;

	private Boolean termWorkDay;

	private Boolean highlight;
	
	private List<ItemModel> itemModels;
	
	public static ItemDTO toDTO( Item model ) {
		
		if( model == null ) {
			return null;
		}
		
		return ItemDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.cod( model.getCod() )
							.seq( model.getSeq() )
							.forFree( model.getForFree() )
							.generic( model.getGeneric() )
							.itemType( ItemTypeDTO.toDTO( model.getItemType() ) )
							.mandatory( model.getMandatory() )
							.description( model.getDescription() )
							.hyperlink( model.getHyperlink() )
							.file( model.getFile() )
							.icon( model.getIcon() )
							.itemModels( model.getItemModels() )
							.responsability( model.getResponsability() )
							.term( model.getTerm() )
							.termWorkDay( model.getTermWorkDay() )
							.highlight( model.getHighlight() )
							.build();
	}

	public static List<ItemDTO> toDTO( List<Item> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( ItemDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
