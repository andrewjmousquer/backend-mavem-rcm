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

import com.portal.dto.PartnerGroupDTO;
import com.portal.model.PartnerGroup;

@ExtendWith(SpringExtension.class)
class PartnerGroupTest {
	
	@Test
	void givenPartnerGroupDTO_whenConvertToEntity_thenReturnPartnerGroup() {
		PartnerGroupDTO dtoMock = new PartnerGroupDTO( 1, "PartnerGroup 1", true );
		PartnerGroup entity = PartnerGroup.toEntity( dtoMock ); 
		
		assertNotNull( entity );
		assertEquals( dtoMock.getId(), entity.getId());
		assertEquals( dtoMock.getName(), entity.getName());
		assertEquals( dtoMock.getActive(), entity.getActive());
	}
	
	@Test
	void givenPartnerGroup_whenConvertToDTO_thenReturnPartnerGroupDTO() {
		PartnerGroup entityMock = new PartnerGroup( 1, "PartnerGroup 1", true );
		PartnerGroupDTO dto = PartnerGroupDTO.toDTO( entityMock ); 
		
		assertNotNull( entityMock );
		assertEquals( entityMock.getId(), dto.getId());
		assertEquals( entityMock.getName(), dto.getName());
		assertEquals( entityMock.getActive(), dto.getActive());
	}
	
	@Test
	void givenListPartnerGroup_whenConvertToDTO_thenReturnListPartnerGroupDTO() {
		
		PartnerGroup entity1 = new PartnerGroup( 1, "PartnerGroup 1", true );
		PartnerGroup entity2 = new PartnerGroup( 2, "PartnerGroup 2", true );
		
		List<PartnerGroup> entities = Arrays.asList( entity1, entity2 );
		
		PartnerGroupDTO dto1 = new PartnerGroupDTO( 1, "PartnerGroup 1", true );
		PartnerGroupDTO dto2 = new PartnerGroupDTO( 2, "PartnerGroup 2", true );
		
		List<PartnerGroupDTO> dtos = PartnerGroupDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullPartnerGroupDTO_whenConvertToEntity_thenReturnNull() {
		PartnerGroup entity = PartnerGroup.toEntity( null ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullPartnerGroup_whenConvertToDTO_thenReturnNull() {
		PartnerGroup entity = null;
		PartnerGroupDTO dto = PartnerGroupDTO.toDTO( entity );
		assertNull( dto );
	}
}
