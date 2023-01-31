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

import com.portal.dto.BankDTO;
import com.portal.model.Bank;

@ExtendWith(SpringExtension.class)
class BankTest {
	
	@Test
	void givenBankDTO_whenConvertToEntity_thenReturnBank() {
		BankDTO dto = new BankDTO( 1, "Bank 1", "Code1", true );
		Bank entity = Bank.toEntity( dto ); 
		
		assertNotNull( entity );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getCode(), dto.getCode());
	}
	
	@Test
	void givenBank_whenConvertToDTO_thenReturnBankDTO() {
		Bank entity = new Bank( 1, "Bank 1", "Code1", true );
		BankDTO dto = BankDTO.toDTO( entity ); 
		
		assertNotNull( dto );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getCode(), dto.getCode());
	}
	
	@Test
	void givenListBank_whenConvertToDTO_thenReturnListBankDTO() {
		
		Bank entity1 = new Bank( 1, "Bank 1", "Code 1", true );
		Bank entity2 = new Bank( 2, "Bank 2", "Code 2", true );
		
		List<Bank> entities = Arrays.asList( entity1, entity2 );
		
		BankDTO dto1 = new BankDTO( 1, "Bank 1", "Code 1", true );
		BankDTO dto2 = new BankDTO( 2, "Bank 2", "Code 2", true );
		
		List<BankDTO> dtos = BankDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullBankDTO_whenConvertToEntity_thenReturnNull() {
		Bank entity = Bank.toEntity( null ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullBank_whenConvertToDTO_thenReturnNull() {
		Bank entity = null;
		BankDTO dto = BankDTO.toDTO( entity );
		assertNull( dto );
	}
}
