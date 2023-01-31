package com.portal.model;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

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
public class ProposalDetailVehicle {

	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ProposalDetail proposalDetail;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private PriceProduct priceProduct;
	
	private VehicleModel vehicle;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Model model;
	
	@Size(max = 100, groups = {OnUpdate.class, OnSave.class})
	private String version;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer modelYear;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double productAmountDiscount;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double productPercentDiscount;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double productFinalPrice;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double overPrice;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double overPricePartnerDiscountAmount;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double overPricePartnerDiscountPercent;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double priceDiscountAmount;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double priceDiscountPercent;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double totalAmount;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double totalTaxAmount;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double totalTaxPercent;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer standardTermDays;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer agreedTermDays;
	
	private Boolean futureDelivery;

}