package com.portal.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.portal.dto.PaymentRuleDTO;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;

@ExtendWith(SpringExtension.class)
class PaymentRuleTest {
	
	@Test
	void givenPaymentRuleDTO_whenConvertToEntity_thenReturnPaymentRule() {
//		PaymentRuleDTO dto = new PaymentRuleDTO( 1, 1, 1d, PaymentMethodDTO.builder().id(1).build().getId(), "PYR 1", true, true );
//		PaymentMethod paymentMethod =  PaymentMethod.toEntity(PaymentMethodDTO.builder().id(1).build());
//		PaymentRule entity = PaymentRule.toEntity( dto, paymentMethod);
//		
//		assertNotNull( entity );
//		assertEquals( entity.getId(), dto.getId());
//		assertEquals( entity.getInstallments(), dto.getInstallments());
//		assertEquals( entity.getTax(), dto.getTax());
//		assertEquals( entity.getPaymentMethod().getId(), dto.getPaymentMethod());
	}
	
	@Test
	void givenPaymentRule_whenConvertToDTO_thenReturnPaymentRuleDTO() {
		PaymentRule entity = new PaymentRule( 1, 1, 1d, PaymentMethod.builder().id(1).build(), "PYR 1", true, true);
		PaymentRuleDTO dto = PaymentRuleDTO.toDTO( entity ); 
		
		assertNotNull( dto );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getInstallments(), dto.getInstallments());
		assertEquals( entity.getTax(), dto.getTax());
		assertEquals( entity.getPaymentMethod().getId(), dto.getPaymentMethod());
	}
	
	@Test
	void givenListPaymentRule_whenConvertToDTO_thenReturnListPaymentRuleDTO() {
		
		PaymentRule entity1 = new PaymentRule( 1, 1, 1d, PaymentMethod.builder().id(1).build(), "PYR 1", true, true);
		PaymentRule entity2 = new PaymentRule( 2, 2, 2d, PaymentMethod.builder().id(2).build(), "PYR 2", true, true);
		
		List<PaymentRule> entities = Arrays.asList( entity1, entity2 );
		
		//PaymentRuleDTO dto1 = new PaymentRuleDTO( 1, 1, 1d, PaymentMethodDTO.builder().id(1).build().getId(), "PYR 1", true, true );
		//PaymentRuleDTO dto2 = new PaymentRuleDTO( 2, 2, 2d, PaymentMethodDTO.builder().id(2).build().getId(), "PYR 2", true, true );
		
		List<PaymentRuleDTO> dtos = PaymentRuleDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        //assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullPaymentRuleDTO_whenConvertToEntity_thenReturnNull() {
//		PaymentRule entity = PaymentRule.toEntity( null , null);
//		assertNull( entity );
	}
	
	@Test
	void givenNullPaymentRule_whenConvertToDTO_thenReturnNull() {
		PaymentRule entity = null;
		PaymentRuleDTO dto = PaymentRuleDTO.toDTO( entity );
		assertNull( dto );
	}
}
