package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PriceItemModel;

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
public class PriceItemModelDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private Double price;          
	
	private Boolean allModels;
	
	private Boolean allBrands;
	
	private PriceListDTO priceList;
	
	private ItemDTO item;
	
	private ItemModelDTO itemModel;
	
	private BrandDTO brand;
	
	public static PriceItemModelDTO toDTO( PriceItemModel entity ) {
		
		if( entity == null ) {
			return null;
		}
		
		return PriceItemModelDTO.builder()
									.id( entity.getId() )
									.price( entity.getPrice() )
									.allBrands( entity.getAllBrands() )
									.allModels( entity.getAllModels() )
									.priceList( PriceListDTO.toDTO( entity.getPriceList() ) )
									.item( ItemDTO.toDTO( entity.getItem() ) )
									.itemModel( ItemModelDTO.toDTO( entity.getItemModel() ) )
									.brand( BrandDTO.toDTO( entity.getBrand() ) )
									.build();
	}

	public static List<PriceItemModelDTO> toDTO( List<PriceItemModel> entities ) {
		
		if( entities == null ) {
			return null;
		}
		
		return entities.stream()
						.map( PriceItemModelDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
	
}