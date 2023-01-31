package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PriceItem;

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
public class PriceItemDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private Double price;
	
	private ItemDTO item; 

	private PriceListDTO priceList; 
	
	public static PriceItemDTO toDTO( PriceItem priceItem ) {
		
		if( priceItem == null ) {
			return null;
		}
		
		return PriceItemDTO.builder()
							.id( priceItem.getId() )
							.price( priceItem.getPrice() )
							.item( ItemDTO.toDTO( priceItem.getItem() ) )
							.priceList( PriceListDTO.toDTO( priceItem.getPriceList() ) )
							.build();
	}

	public static List<PriceItemDTO> toDTO( List<PriceItem> priceItens ) {
		
		if( priceItens == null ) {
			return null;
		}
		
		return priceItens.stream()
						.map( PriceItemDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
	
}