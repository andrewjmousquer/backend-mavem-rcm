package com.portal.dto;

import com.portal.model.Item;
import com.portal.model.ItemType;
import com.portal.model.PriceItemModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProposalItemProductDTO {

	private ItemType itemType;
	
	private Item item;
	
	private PriceItemModel priceItemModel;
	
	
}
