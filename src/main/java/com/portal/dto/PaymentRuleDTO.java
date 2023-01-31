package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.PaymentRule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentRuleDTO {

	@EqualsAndHashCode.Include
	private Integer id;

	private Integer installments;

	private Double tax;

	private PaymentMethodDTO paymentMethod;

	private String name;

	private Boolean active;

	private Boolean preApproved;



    public static PaymentRuleDTO toDTO( PaymentRule model ) {

		if( model == null ) {
			return null;
		}

		
		return PaymentRuleDTO.builder()
							.id( model.getId() )
							.installments( model.getInstallments() )
							.tax( model.getTax() )
							.paymentMethod( PaymentMethodDTO.toDTO(model.getPaymentMethod()) )
							.name( model.getName() )
							.active( model.getActive() )
							.preApproved( model.getPreApproved() )
							.build();
	}

	public static List<PaymentRuleDTO> toDTO( List<PaymentRule> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( PaymentRuleDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
