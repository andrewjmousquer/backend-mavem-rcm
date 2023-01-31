package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.PositiveOrZero;

import com.portal.dto.PriceItemDTO;
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
public class PriceItem {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@PositiveOrZero(groups = {OnUpdate.class, OnSave.class}, message = "Não é permitido valor zero ou negativo no item")
	private Double price;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Item item; 
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private PriceList priceList; 
	
	public static PriceItem toEntity( PriceItemDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return PriceItem.builder()
							.id( dto.getId() )
							.price( dto.getPrice() )
							.item( Item.toEntity( dto.getItem() ) )
							.priceList( PriceList.toEntity( dto.getPriceList() ) )
							.build();
	}
	
	public static List<PriceItem> toEntity( List<PriceItemDTO> dtos ) {
		
		if( dtos == null ) {
			return null;
		}
		
		return dtos.stream()
						.map( PriceItem::toEntity )
						.collect( Collectors.toList() );	
		
	}
}