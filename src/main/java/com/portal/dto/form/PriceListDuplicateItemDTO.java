package com.portal.dto.form;

import com.portal.dto.PriceListDTO;

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
@EqualsAndHashCode
public class PriceListDuplicateItemDTO {

	@EqualsAndHashCode.Exclude
	private PriceListDTO priceList;
	
	private Integer channelId;
	
	private Integer partnerId;
	
	private Integer productModelId;
	
	private Integer itemId;
	
	private Integer itemModelId;
	
	private Integer brandId;
	
	private boolean allBrands;
	
	private boolean allModels;
}
