package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.PositiveOrZero;

import com.portal.dto.PriceItemModelDTO;
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
public class PriceItemModel {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@PositiveOrZero(groups = {OnUpdate.class, OnSave.class}, message = "Não é permitido valor zero ou negativo no item")
	private Double price;          
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean allModels;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean allBrands;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private PriceList priceList;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Item item;

	private ItemModel itemModel;
	
	private Brand brand;
	
	public static PriceItemModel toEntity( PriceItemModelDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return PriceItemModel.builder()
									.id( dto.getId() )
									.price( dto.getPrice() )
									.allBrands( dto.getAllBrands() )
									.allModels( dto.getAllModels() )
									.priceList( PriceList.toEntity( dto.getPriceList() ) )
									.item( Item.toEntity( dto.getItem() ) )
									.itemModel( ItemModel.toEntity( dto.getItemModel() ) )
									.brand( Brand.toEntity( dto.getBrand() ) )
									.build();
	}
	
	public static List<PriceItemModel> toEntity( List<PriceItemModelDTO> dtos ) {
		
		if( dtos == null ) {
			return null;
		}
		
		return dtos.stream()
						.map( PriceItemModel::toEntity )
						.collect( Collectors.toList() );	
		
	}
}