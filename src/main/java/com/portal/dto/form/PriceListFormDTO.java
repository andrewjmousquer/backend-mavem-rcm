package com.portal.dto.form;

import java.util.List;

import com.portal.dto.PartnerDTO;
import com.portal.dto.PriceItemDTO;
import com.portal.dto.PriceItemModelDTO;
import com.portal.dto.PriceListDTO;
import com.portal.dto.PriceProductDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class PriceListFormDTO {
	
	private PriceListDTO priceList;
	
	private List<PriceProductDTO> products; 
	
	private List<PriceItemDTO> itens;
	
	private List<PriceItemModelDTO> itensModel;
	
	private List<PartnerDTO> partners;
}
