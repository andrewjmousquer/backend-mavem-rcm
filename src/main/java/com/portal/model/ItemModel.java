package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.dto.ItemModelDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemModel {
	
	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer modelYearStart;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer modelYearEnd;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@JsonBackReference
	private Item item;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Model model;
	
	 public static ItemModel toEntity(ItemModelDTO dto) {
		if(dto == null){
			return null;
		}
		
		return ItemModel.builder()
							.id( dto.getId() )
							.modelYearStart( dto.getModelYearStart() )
							.modelYearEnd( dto.getModelYearEnd() )
							.item( Item.toEntity( dto.getItem() ) )
							.model( Model.toEntity( dto.getModel() ) ) 
							.build();
	 }
	 
	 public static List<ItemModel> toEntity(List<ItemModelDTO> dtos) {

		if( dtos == null ) {
			return null;
		}

		return dtos.stream()
				.map( ItemModel::toEntity )
				.collect( Collectors.toList() );
	}
	
}
