package com.portal.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.portal.dto.PaymentMethodDTO;
import com.portal.model.PaymentMethod;

@ExtendWith(SpringExtension.class)
class PaymentMethodTest {
	
	@Test
	void givenPaymentMethodDTO_whenConvertToEntity_thenReturnPaymentMethod() {
		PaymentMethodDTO dto = new PaymentMethodDTO( 1, "PaymentMethod 1", true );
		PaymentMethod entity = PaymentMethod.toEntity( dto ); 
		
		assertNotNull( entity );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getActive(), dto.getActive());
	}
	
	@Test
	void givenPaymentMethod_whenConvertToDTO_thenReturnPaymentMethodDTO() {
		PaymentMethod entity = new PaymentMethod( 1, "PaymentMethod 1", true );
		PaymentMethodDTO dto = PaymentMethodDTO.toDTO( entity ); 
		
		assertNotNull( dto );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getActive(), dto.getActive());
	}
	
	@Test
	void givenListPaymentMethod_whenConvertToDTO_thenReturnListPaymentMethodDTO() {
		
		PaymentMethod entity1 = new PaymentMethod( 1, "PaymentMethod 1", true );
		PaymentMethod entity2 = new PaymentMethod( 2, "PaymentMethod 2", true );
		
		List<PaymentMethod> entities = Arrays.asList( entity1, entity2 );
		
		PaymentMethodDTO dto1 = new PaymentMethodDTO( 1, "PaymentMethod 1", true );
		PaymentMethodDTO dto2 = new PaymentMethodDTO( 2, "PaymentMethod 2", true );
		
		List<PaymentMethodDTO> dtos = PaymentMethodDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullPaymentMethodDTO_whenConvertToEntity_thenReturnNull() {
		PaymentMethod entity = PaymentMethod.toEntity( null ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullPaymentMethod_whenConvertToDTO_thenReturnNull() {
		PaymentMethod entity = null;
		PaymentMethodDTO dto = PaymentMethodDTO.toDTO( entity );
		assertNull( dto );
	}
}
