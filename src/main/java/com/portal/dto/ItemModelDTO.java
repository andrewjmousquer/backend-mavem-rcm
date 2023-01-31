package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

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
public class ItemModelDTO {
	@EqualsAndHashCode.Include
	private Integer id;
	private Integer modelYearStart;
	private Integer modelYearEnd;
	private ItemDTO item;
	private ModelDTO model;
	
	 public static ItemModelDTO toDTO(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        
        return ItemModelDTO.builder()
        					.id( itemModel.getId() )
        					.modelYearStart( itemModel.getModelYearStart() )
        					.modelYearEnd( itemModel.getModelYearEnd() )
        					.item( ItemDTO.toDTO( itemModel.getItem() ) )
        					.model( ModelDTO.toDTO( itemModel.getModel() ) )
        					.build();
    }
	 
	public static List<ItemModelDTO> toDTO(List<ItemModel> itemModel) {
		if (itemModel == null) {
			return null;
		}
		
		return itemModel.stream()
							.map(ItemModelDTO::toDTO)
							.collect(Collectors.toList());
	}

}
