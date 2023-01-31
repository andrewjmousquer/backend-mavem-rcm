package com.portal.dto.form;

import com.portal.dto.PriceListDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PriceListFormSearchDTO {
	private PriceListDTO priceList;
	private Integer qtdPartners;
}
