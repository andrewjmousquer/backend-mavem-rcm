package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;

import com.portal.dto.PriceProductDTO;
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
public class PriceProduct {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@Positive(groups = {OnUpdate.class, OnSave.class}, message = "Não é permitido valor zero ou negativo no produto")
	private Double price;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private PriceList priceList;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ProductModel productModel;
	
	public static PriceProduct toEntity( PriceProductDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return PriceProduct.builder()
								.id( dto.getId() )
								.price( dto.getPrice() )
								.priceList( PriceList.toEntity( dto.getPriceList() ) )
								.productModel( ProductModel.toEntity( dto.getProductModel() ) )
								.build();
	}
	
	public static List<PriceProduct> toEntity( List<PriceProductDTO> dtos ) {
		
		if( dtos == null ) {
			return null;
		}
		
		return dtos.stream()
						.map( PriceProduct::toEntity )
						.collect( Collectors.toList() );	
		
	}
	
}