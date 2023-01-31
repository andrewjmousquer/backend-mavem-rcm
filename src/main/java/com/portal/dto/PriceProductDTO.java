package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PriceProduct;

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
public class PriceProductDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private Double price;
	
	private PriceListDTO priceList;
	
	private ProductModelDTO productModel;
	
	public static PriceProductDTO toDTO( PriceProduct entity ) {
		
		if( entity == null ) {
			return null;
		}
		
		return PriceProductDTO.builder()
							.id( entity.getId() )
							.price( entity.getPrice() )
							.priceList( PriceListDTO.toDTO( entity.getPriceList() ) )
							.productModel( ProductModelDTO.toDTO( entity.getProductModel() ) )
							.build();
	}

	public static List<PriceProductDTO> toDTO( List<PriceProduct> entities ) {
		
		if( entities == null ) {
			return null;
		}
		
		return entities.stream()
						.map( PriceProductDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
	
}