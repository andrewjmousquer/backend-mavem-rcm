package com.portal.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.PositiveOrZero;

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
public class ProposalPayment {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@PositiveOrZero(groups = {OnUpdate.class, OnSave.class})
	private Double paymentAmount;
	
	private Date dueDate;
	
	@PositiveOrZero(groups = {OnUpdate.class, OnSave.class})
	private Double installmentAmount;

	private Double interest;
	 
	private PaymentMethod paymentMethod;
	
	private PaymentRule paymentRule;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ProposalDetail proposalDetail;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier payer;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier event;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean preApproved;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean	antecipatedBilling;

	private Integer position;

	private Integer quantityDays;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean carbonBilling;
}