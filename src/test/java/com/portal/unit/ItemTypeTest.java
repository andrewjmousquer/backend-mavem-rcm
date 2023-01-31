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

import com.portal.dto.ItemTypeDTO;
import com.portal.model.ItemType;

@ExtendWith(SpringExtension.class)
class ItemTypeTest {
	
	@Test
	void givenItemTypeDTO_whenConvertToEntity_thenReturnItemType() {
		ItemTypeDTO dto = new ItemTypeDTO( 1, "ItemType 1", true, false, 1, 1, "11", false, false );
		ItemType entity = ItemType.toEntity( dto ); 
		
		assertNotNull( entity );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getMandatory(), dto.getMandatory());
		assertEquals( entity.getMulti(), dto.getMulti());
		assertEquals( entity.getSeq(), dto.getSeq());
	}
	
	@Test
	void givenItemType_whenConvertToDTO_thenReturnItemTypeDTO() {
		ItemType entity = new ItemType( 1, "ItemType 1", true, false, 1 );
		ItemTypeDTO dto = ItemTypeDTO.toDTO( entity ); 
		
		assertNotNull( dto );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName());
		assertEquals( entity.getMandatory(), dto.getMandatory());
		assertEquals( entity.getMulti(), dto.getMulti());
		assertEquals( entity.getSeq(), dto.getSeq());
	}
	
	@Test
	void givenListItemType_whenConvertToDTO_thenReturnListItemTypeDTO() {
		
		ItemType entity1 = new ItemType( 1, "ItemType 1", true, false, 1);
		ItemType entity2 = new ItemType( 2,  "ItemType 2", true, false, 1 );
		
		List<ItemType> entities = Arrays.asList( entity1, entity2 );
		
		ItemTypeDTO dto1 = new ItemTypeDTO( 1, "ItemType 1", true, false, 1, 1, "11", false, false );
		ItemTypeDTO dto2 = new ItemTypeDTO( 2, "ItemType 2", true, false, 1, 1, "11", false, false);
		
		List<ItemTypeDTO> dtos = ItemTypeDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullItemTypeDTO_whenConvertToEntity_thenReturnNull() {
		ItemType entity = ItemType.toEntity( null ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullItemType_whenConvertToDTO_thenReturnNull() {
		ItemType entity = null;
		ItemTypeDTO dto = ItemTypeDTO.toDTO( entity );
		assertNull( dto );
	}
}
