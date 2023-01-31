package com.portal.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Setter
@Getter
public class PortionModel {

	public PortionModel(Integer name) {
		this.name = name;
	}

	public PortionModel(Integer name, Classifier paymentType) {
		this.name = name;
		this.paymentType = paymentType;
	}

	public PortionModel(Classifier classifierModel) {
		this.paymentType = classifierModel;
	}

	private Integer id;

	private Integer name;

	private BigDecimal tax;

	private Classifier paymentType;

	public static Object toEntity(PortionModel dto) {
		if (dto == null) {
			return null;
		}

		return new PortionModel(dto.getName(), dto.getPaymentType());
	}
}
