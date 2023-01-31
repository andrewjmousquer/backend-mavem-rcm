package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PaymentMethod;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentMethodDTO {

	@EqualsAndHashCode.Include
	private Integer id;

	private String name;

	private Boolean active;

	public PaymentMethodDTO(Integer id, String name, Boolean active) {
		this.id = id;
		this.name = name;
		this.active = active;
	}

	public static PaymentMethodDTO toDTO(PaymentMethod model) {

		if (model == null) {
			return null;
		}

		return PaymentMethodDTO.builder()
				.id(model.getId())
				.name(model.getName())
				.active(model.getActive())
				.build();
	}

	public static List<PaymentMethodDTO> toDTO( List<PaymentMethod> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( PaymentMethodDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
