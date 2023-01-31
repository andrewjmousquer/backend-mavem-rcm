package com.portal.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.ModelItemCost;

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
public class ModelItemCostDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	private Double price;          
	private Boolean allModels;
	private Boolean allBrands;
	private ItemDTO item;
	private ItemModelDTO itemModel;
	private BrandDTO brand;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private LocalDateTime dateFilter;
	private Boolean hasValidationError;
	
	public static ModelItemCostDTO toDTO( ModelItemCost entity ) {
		
		if( entity == null ) {
			return null;
		}
		
		return ModelItemCostDTO.builder()
									.id( entity.getId() )
									.price( entity.getPrice() )
									.allBrands( entity.getAllBrands() )
									.allModels( entity.getAllModels() )
									.item( ItemDTO.toDTO( entity.getItem() ) )
									.itemModel( ItemModelDTO.toDTO( entity.getItemModel() ) )
									.brand( BrandDTO.toDTO( entity.getBrand() ) )
									.startDate( entity.getStartDate() )
									.endDate( entity.getEndDate() )
									.dateFilter( entity.getDateFilter() )
									.hasValidationError ( entity.getHasValidationError() )
									.build();
	}

	public static List<ModelItemCostDTO> toDTO( List<ModelItemCost> entities ) {
		
		if( entities == null ) {
			return null;
		}
		
		return entities.stream()
						.map( ModelItemCostDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
	
}
