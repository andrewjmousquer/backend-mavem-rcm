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

import com.portal.dto.ItemDTO;
import com.portal.dto.ItemTypeDTO;
import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemType;

@ExtendWith(SpringExtension.class)
class ItemTest {
	
	private static ItemTypeDTO itemTypeDTO = new ItemTypeDTO( 1, "ItemType 1", true, false, 1, 1, "11", false, false );
	
	@Test
	void givenItemDTO_whenConvertToEntity_thenReturnItem() {
		ItemDTO dto = new ItemDTO(1, "Item 1", "1", 1, false, false, new Classifier(24), itemTypeDTO, "DESC", "HTTP", "/tmp/", null, null, null, null, null, null);

		Item entity = Item.toEntity( dto ); 
		
		assertNotNull( entity );
		assertEquals( dto.getId(), entity.getId());
		assertEquals( dto.getName(), entity.getName() );
		assertEquals( dto.getCod(), entity.getCod() );
		assertEquals( dto.getSeq(), entity.getSeq() );
		assertEquals( dto.getForFree(), entity.getForFree() );
		assertEquals( dto.getGeneric(), entity.getGeneric() );
		assertNotNull( dto.getItemType() );
		assertEquals( dto.getItemType().getId(), entity.getItemType().getId() );
		assertNotNull( dto.getMandatory() );
		assertEquals( dto.getMandatory(), entity.getMandatory() );
		assertEquals( dto.getDescription(), entity.getDescription() );
		assertEquals( dto.getHyperlink(), entity.getHyperlink() );
	}
	
	@Test
	void givenItem_whenConvertToDTO_thenReturnItemDTO() {
		Item entity = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
		ItemDTO dto = ItemDTO.toDTO( entity ); 
		
		assertNotNull( entity );
		assertEquals( entity.getId(), dto.getId());
		assertEquals( entity.getName(), dto.getName() );
		assertEquals( entity.getCod(), dto.getCod() );
		assertEquals( entity.getSeq(), dto.getSeq() );
		assertEquals( entity.getForFree(), dto.getForFree() );
		assertEquals( entity.getGeneric(), dto.getGeneric() );
		assertNotNull( entity.getItemType() );
		assertEquals( entity.getItemType().getId(), dto.getItemType().getId() );
		assertNotNull( entity.getMandatory() );
		assertEquals( entity.getMandatory(), dto.getMandatory() );
		assertEquals( entity.getDescription(), dto.getDescription() );
		assertEquals( entity.getHyperlink(), dto.getHyperlink() );
	}
	
	@Test
	void givenListItem_whenConvertToDTO_thenReturnListItemDTO() {
		
		Item entity1 = new Item(1, "Item 1", "200", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
		Item entity2 = new Item(2, "Item 1", "220", 1, false, false, new Classifier(23), ItemType.builder().id(1).build(), "ICON", "DESC", "HTTP", null, null, null, null, null, null);
		
		List<Item> entities = Arrays.asList( entity1, entity2 );
		
		ItemDTO dto1 = new ItemDTO(1, "Item 1", "1", 1, false, false, new Classifier(24), itemTypeDTO, "DESC", "HTTP", "/tmp/", null, null, null, null, null, null);
		ItemDTO dto2 = new ItemDTO(2, "Item 2", "2", 2, false, false, new Classifier(24), itemTypeDTO, "DESC", "HTTP", "/tmp/", null, null, null, null, null, null);
		
		List<ItemDTO> dtos = ItemDTO.toDTO( entities ); 
		
		assertNotNull( dtos );
		assertFalse( dtos.isEmpty() );
        assertThat( dtos, containsInAnyOrder(dto1, dto2) );
	}
	
	@Test
	void givenNullItemDTO_whenConvertToEntity_thenReturnNull() {
		ItemType entity = ItemType.toEntity( null ); 
		assertNull( entity );
	}
	
	@Test
	void givenNullItemType_whenConvertToDTO_thenReturnNull() {
		Item entity = null;
		ItemDTO dto = ItemDTO.toDTO( entity );
		assertNull( dto );
	}
}