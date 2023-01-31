package com.portal.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import com.portal.dto.PaymentRuleDTO;
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
public class PaymentRule {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@PositiveOrZero(groups = {OnUpdate.class, OnSave.class})
	private Integer installments;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double tax;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private PaymentMethod paymentMethod;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String name;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean active;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean preApproved;


	public static PaymentRule toEntity(PaymentRuleDTO dto, PaymentMethod paymentMethod) {

		if (dto == null) {
			return null;
		}

		return PaymentRule.builder()
				.id(dto.getId())
				.installments(dto.getInstallments())
				.tax(dto.getTax())
				.paymentMethod( PaymentMethod.toEntity(dto.getPaymentMethod()))
				.name(dto.getName())
				.active(dto.getActive())
				.preApproved(dto.getPreApproved())
				.build();
	}


	public static PaymentRuleDTO toDTO(PaymentRule model) {

		if (model == null) {
			return null;
		}

		return PaymentRuleDTO.builder()
				.id(model.getId())
				.installments(model.getInstallments())
				.tax(model.getTax())
				//.paymentMethod(model.getPaymentMethod().getId())
				.name(model.getName())
				.active(model.getActive())
				.preApproved(model.getPreApproved())
				.build();
	}

}
